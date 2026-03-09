package com.promptvault.service;

import com.promptvault.dto.AIImprovementResponse;
import com.promptvault.dto.PromptImprovementDTO;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Prompt;
import com.promptvault.model.PromptImprovement;
import com.promptvault.repository.PromptImprovementRepository;
import com.promptvault.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para mejorar prompts usando IA (Groq).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIEnhancementService {

    private final GroqClient groqClient;
    private final PromptRepository promptRepository;
    private final PromptImprovementRepository promptImprovementRepository;

    /**
     * Obtiene el historial de mejoras para un prompt.
     */
    public List<PromptImprovementDTO> getImprovementHistory(Long promptId) {
        if (!promptRepository.existsById(promptId)) {
            throw new ResourceNotFoundException("Prompt", "id", promptId);
        }
        return promptImprovementRepository.findByPromptIdOrderByCreatedAtDesc(promptId)
                .stream()
                .map(this::toImprovementDTO)
                .toList();
    }

    private PromptImprovementDTO toImprovementDTO(PromptImprovement improvement) {
        return PromptImprovementDTO.builder()
                .id(improvement.getId())
                .originalContent(improvement.getOriginalContent())
                .improvedContent(improvement.getImprovedContent())
                .improvements(improvement.getImprovements() != null ? 
                        Arrays.asList(improvement.getImprovements().split("\n")) : 
                        List.of())
                .quality(improvement.getQuality() != null ? improvement.getQuality() : "N/A")
                .completeness(improvement.getCompleteness() != null ? improvement.getCompleteness() : 0)
                .createdAt(improvement.getCreatedAt())
                .build();
    }

    /**
     * Mejora un prompt existente usando Groq API.
     */
    public AIImprovementResponse improvePrompt(Long promptId) {
        log.info("Iniciando mejora para prompt ID: {}", promptId);

        // Obtener prompt
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt", "id", promptId));

        String originalContent = prompt.getContent();

        try {
            // Llamar a Groq
            String groqResponse = groqClient.generateContent(
                buildImprovementPrompt(originalContent, prompt.getCategory()));
            
            if (groqResponse == null || groqResponse.isBlank()) {
                throw new RuntimeException("Respuesta vacia de Groq API");
            }

            AIImprovementResponse response = parseImprovementResponse(originalContent, groqResponse);

            // Actualizar prompt con calidad basada en completeness
            prompt.setContent(response.getImprovedContent());
            prompt.setLastImprovedAt(LocalDateTime.now());
            
            // Actualizar qualityScore basado en el completeness de la mejora
            int completeness = response.getStructureValidation() != null ? 
                response.getStructureValidation().getCompleteness() : 0;
            // Quality score va de 50 (base) a 100 (perfecto)
            int newQualityScore = 50 + (completeness / 2);
            prompt.setQualityScore(newQualityScore);
            
            promptRepository.save(prompt);
            log.info("Prompt actualizado ID: {}, Quality Score: {}", prompt.getId(), newQualityScore);

            // Guardar historial (operacion independiente)
            PromptImprovement improvement = PromptImprovement.builder()
                    .prompt(prompt)
                    .originalContent(originalContent)
                    .improvedContent(response.getImprovedContent())
                    .improvements(response.getImprovements() != null ? 
                        String.join("\n", response.getImprovements()) : "")
                    .quality(response.getStructureValidation() != null ? 
                        response.getStructureValidation().getQuality() : "N/A")
                    .completeness(response.getStructureValidation() != null ? 
                        response.getStructureValidation().getCompleteness() : 0)
                    .build();
            
            promptImprovementRepository.save(improvement);
            log.info("Historial guardado ID: {}", improvement.getId());

            log.info("Mejora completada para prompt ID: {}", promptId);
            return response;

        } catch (IOException e) {
            log.error("Error con Groq API: {}", e.getMessage());
            throw new RuntimeException("Error mejorando prompt: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error en mejora: {}", e.getMessage(), e);
            throw new RuntimeException("Error inesperado: " + e.getMessage(), e);
        }
    }

    private String buildImprovementPrompt(String originalContent, String category) {
        return String.format("""
                Eres un experto en prompt engineering con anos de experiencia optimizando prompts para LLMs.
                
                Tu tarea es transformar el siguiente prompt en uno profesional usando la estructura de 5 secciones.
                
                PROMPT ORIGINAL (categoria: %s):
                ```
                %s
                ```
                
                ESTRUCTURA REQUERIDA - DEBES USAR ESTAS 5 SECCIONES OBLIGATORIAMENTE:
                
                [ROL]
                Define quien debe responder o desde que perspectiva.
                Ejemplo: "Actua como desarrollador senior especializado en..."
                
                [TAREA]
                Explica de forma clara y especifica que debe hacer el modelo.
                - Usa verbos de accion precisos
                - Divide tareas complejas en pasos numerados
                
                [AUDIENCIA]
                Indica para quien es el resultado.
                Ejemplo: "Desarrolladores junior/senior", "Usuarios tecnicos"
                
                [FORMATO]
                Define como presentar la salida.
                Ejemplos: "Codigo comentado", "Lista de pasos", "Markdown estructurado"
                
                [CONTEXTO/DETALLES]
                Incluye informacion relevante: tecnologias, restricciones, ejemplos.
                
                FORMATO DE RESPUESTA EXACTO:
                1. Primero el prompt mejorado con las 5 secciones claramente marcadas
                2. Luego escribe exactamente "---MEJORAS---"
                3. Finalmente lista las mejoras realizadas (una por linea con "- ")
                
                EJEMPLO DE FORMATO ESPERADO:
                
                [ROL]
                Actua como desarrollador senior...
                
                [TAREA]
                1. Analiza el codigo...
                2. Identifica el problema...
                
                [AUDIENCIA]
                Este analisis es para desarrolladores...
                
                [FORMATO]
                - Codigo corregido con comentarios
                - Explicacion paso a paso
                
                [CONTEXTO/DETALLES]
                - Framework: Spring Boot 3.2
                - Java 17
                
                ---MEJORAS---
                - Anadida estructura de 5 secciones
                - Mejorado el rol especifico
                - Detallado el formato de salida
                
                IMPORTANTE: Empieza DIRECTAMENTE con [ROL], sin introducciones.
                """, category, originalContent);
    }

    private AIImprovementResponse parseImprovementResponse(String originalContent, String groqResponse) {
        String cleanResponse = groqResponse.trim();
        
        // Limpiar bloques Markdown
        if (cleanResponse.startsWith("```") && cleanResponse.endsWith("```")) {
            int firstNewline = cleanResponse.indexOf("\n");
            int lastBackticks = cleanResponse.lastIndexOf("```");
            if (firstNewline > 0 && lastBackticks > firstNewline) {
                cleanResponse = cleanResponse.substring(firstNewline, lastBackticks).trim();
            }
        }

        String[] parts = cleanResponse.split("---MEJORAS---");
        String improvedContent = parts[0].trim();
        
        List<String> improvements = parts.length >= 2 ?
            Arrays.stream(parts[1].split("\n"))
                .map(String::trim)
                .filter(line -> line.startsWith("-") || line.startsWith("*"))
                .map(line -> line.replaceAll("^[-*]\\s*", ""))
                .filter(line -> !line.isBlank())
                .toList() :
            List.of("Prompt mejorado");

        // Limpiar Markdown del contenido
        if (improvedContent.startsWith("```")) {
            int firstNewline = improvedContent.indexOf("\n");
            if (firstNewline > 0) improvedContent = improvedContent.substring(firstNewline).trim();
            if (improvedContent.endsWith("```")) 
                improvedContent = improvedContent.substring(0, improvedContent.length() - 3).trim();
        }

        // Validar secciones
        boolean hasRol = improvedContent.contains("[ROL]");
        boolean hasTarea = improvedContent.contains("[TAREA]");
        boolean hasAudiencia = improvedContent.contains("[AUDIENCIA]");
        boolean hasFormato = improvedContent.contains("[FORMATO]");
        boolean hasContexto = improvedContent.contains("[CONTEXTO");

        int sectionsPresent = (hasRol ? 1 : 0) + (hasTarea ? 1 : 0) + 
                              (hasAudiencia ? 1 : 0) + (hasFormato ? 1 : 0) + (hasContexto ? 1 : 0);
        int completeness = (sectionsPresent * 100) / 5;

        String quality = completeness == 100 ? "Excelente" :
                         completeness >= 80 ? "Buena" :
                         completeness >= 60 ? "Mejorable" : "Incompleta";

        var validation = AIImprovementResponse.StructureValidation.builder()
                .hasRol(hasRol).hasTarea(hasTarea).hasAudiencia(hasAudiencia)
                .hasFormato(hasFormato).hasContexto(hasContexto)
                .completeness(completeness).quality(quality).build();

        return AIImprovementResponse.builder()
                .originalContent(originalContent)
                .improvedContent(improvedContent)
                .improvements(improvements)
                .structureValidation(validation)
                .build();
    }
}
