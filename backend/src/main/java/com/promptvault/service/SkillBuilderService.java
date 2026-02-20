package com.promptvault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.SkillBuildRequest;
import com.promptvault.dto.SkillBuildResult;
import com.promptvault.dto.SkillBuildResult.GeneratedSkill;
import com.promptvault.model.Skill;
import com.promptvault.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillBuilderService {

    private final GroqClient groqClient;
    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;

    public SkillBuildResult buildSkill(SkillBuildRequest request) {
        Instant start = Instant.now();
        log.info("Building skill for objective: {}", request.getObjective());

        String prompt = buildMetaPrompt(request);
        String rawResponse;
        try {
            rawResponse = groqClient.generateContent(prompt);
        } catch (Exception e) {
            log.error("Error calling Groq: {}", e.getMessage());
            throw new RuntimeException("Error llamando a la IA de Groq: " + e.getMessage());
        }

        GeneratedSkill generated = parseSkillFromResponse(rawResponse);
        long elapsed = Instant.now().toEpochMilli() - start.toEpochMilli();

        Long savedId = null;
        if (request.isSaveToDatabase() && generated != null) {
            savedId = saveSkillToDatabase(generated);
        }

        return SkillBuildResult.builder()
                .skill(generated)
                .modelUsed("llama-3.3-70b-versatile")
                .generationTimeMs(elapsed)
                .savedSkillId(savedId)
                .build();
    }

    /**
     * El meta-prompt: un prompt que genera prompts.
     * Este es el corazón del Skill Builder.
     */
    private String buildMetaPrompt(SkillBuildRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Eres un experto en Prompt Engineering especializado en crear templates de prompts reutilizables.
            
            Tu tarea es generar una SKILL COMPLETA en formato JSON a partir de la descripción del usuario.
            Una Skill es un template de prompt parametrizable que los desarrolladores pueden reutilizar.
            
            OBJETIVO DEL USUARIO: %s
            """.formatted(request.getObjective()));

        if (request.getTargetAudience() != null && !request.getTargetAudience().isBlank()) {
            sb.append("AUDIENCIA OBJETIVO: ").append(request.getTargetAudience()).append("\n");
        }

        if (request.getDesiredOutputFormat() != null && !request.getDesiredOutputFormat().isBlank()) {
            sb.append("FORMATO DE OUTPUT DESEADO: ").append(request.getDesiredOutputFormat()).append("\n");
        }

        if (request.getExampleInputs() != null && !request.getExampleInputs().isEmpty()) {
            sb.append("\nEJEMPLOS DE INPUT QUE RECIBIRÁ LA SKILL:\n");
            request.getExampleInputs().forEach(ex -> sb.append("- ").append(ex).append("\n"));
        }

        sb.append("""
            
            REGLAS para el template:
            - Usa {{PARAMETRO}} para los parámetros que variará el usuario
            - El template debe ser profesional, específico y accionable
            - Incluye instrucciones de ROL, TAREA, FORMATO DE RESPUESTA y CONTEXTO
            - Los nombres de parámetros deben ser en MAYÚSCULAS y descriptivos
            
            Responde ÚNICAMENTE con este JSON (sin markdown, sin explicaciones):
            {
              "name": "Nombre corto y descriptivo de la skill (máx 50 chars)",
              "description": "Descripción de qué hace la skill (máx 200 chars)",
              "template": "El template completo con {{PARAMETROS}}",
              "parameters": ["PARAM1", "PARAM2"],
              "parameterDescriptions": {
                "PARAM1": "Descripción de qué va en este parámetro"
              },
              "exampleOutput": "Ejemplo de cómo se vería el output cuando se use esta skill",
              "category": "%s",
              "estimatedQualityScore": 85
            }
            """.formatted(request.getCategory() != null ? request.getCategory() : "development"));

        return sb.toString();
    }

    private GeneratedSkill parseSkillFromResponse(String rawResponse) {
        try {
            // Limpiar posibles markdown blocks
            String cleaned = rawResponse
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            // Extraer solo el bloque JSON si hay texto extra
            int start = cleaned.indexOf('{');
            int end = cleaned.lastIndexOf('}');
            if (start >= 0 && end > start) {
                cleaned = cleaned.substring(start, end + 1);
            }

            JsonNode node = objectMapper.readTree(cleaned);

            List<String> parameters = new ArrayList<>();
            node.path("parameters").forEach(p -> parameters.add(p.asText()));

            Map<String, String> paramDescs = new LinkedHashMap<>();
            node.path("parameterDescriptions").fields()
                .forEachRemaining(e -> paramDescs.put(e.getKey(), e.getValue().asText()));

            return GeneratedSkill.builder()
                    .name(node.path("name").asText("Generated Skill"))
                    .description(node.path("description").asText(""))
                    .template(node.path("template").asText(""))
                    .parameters(parameters)
                    .parameterDescriptions(paramDescs)
                    .exampleOutput(node.path("exampleOutput").asText(""))
                    .category(node.path("category").asText("development"))
                    .estimatedQualityScore(node.path("estimatedQualityScore").asInt(70))
                    .build();

        } catch (Exception e) {
            log.error("Error parsing skill from Groq response: {}", e.getMessage());
            log.debug("Raw response was: {}", rawResponse);

            // Devolver skill mínima con el template raw para no perder el trabajo de la IA
            return GeneratedSkill.builder()
                    .name("Generated Skill (review needed)")
                    .description("Skill generada - revisar formato manualmente")
                    .template(rawResponse)
                    .parameters(List.of())
                    .parameterDescriptions(Map.of())
                    .exampleOutput("")
                    .category("development")
                    .estimatedQualityScore(50)
                    .build();
        }
    }

    private Long saveSkillToDatabase(GeneratedSkill generated) {
        try {
            // Preparar parámetros en el formato JSON esperado por Skill.java
            List<Map<String, Object>> skillParams = new ArrayList<>();
            for (String pName : generated.getParameters()) {
                Map<String, Object> param = new HashMap<>();
                param.put("name", pName.toLowerCase());
                param.put("type", "text");
                param.put("description", generated.getParameterDescriptions().getOrDefault(pName, "Parámetro " + pName));
                param.put("required", true);
                skillParams.add(param);
            }

            Skill skill = Skill.builder()
                    .name(generated.getName())
                    .description(generated.getDescription())
                    .category(generated.getCategory())
                    .promptTemplate(generated.getTemplate())
                    .parameters(objectMapper.writeValueAsString(skillParams))
                    .exampleOutput(generated.getExampleOutput())
                    .usageCount(0)
                    .difficultyLevel("intermediate")
                    .build();

            Skill saved = skillRepository.save(skill);
            log.info("Saved generated skill with id: {}", saved.getId());
            return saved.getId();
        } catch (Exception e) {
            log.error("Error saving generated skill: {}", e.getMessage());
            return null;
        }
    }
}
