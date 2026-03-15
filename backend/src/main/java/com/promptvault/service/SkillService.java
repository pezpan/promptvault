package com.promptvault.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.GeneratePromptRequest;
import com.promptvault.dto.SkillCreateRequest;
import com.promptvault.dto.SkillDTO;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.Skill;
import com.promptvault.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillService {

    private final SkillRepository skillRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SkillDTO createSkill(SkillCreateRequest request) {
        // Convertir List<String> parameters a JSON array de SkillParameter objects
        String parametersJson = "[]";
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            List<SkillDTO.SkillParameter> skillParams = request.getParameters().stream()
                    .map(name -> SkillDTO.SkillParameter.builder().name(name).build())
                    .collect(Collectors.toList());
            try {
                parametersJson = objectMapper.writeValueAsString(skillParams);
            } catch (Exception e) {
                log.error("Error serializando parametros: {}", e.getMessage());
            }
        }

        Skill skill = Skill.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .promptTemplate(request.getContent())
                .parameters(parametersJson)
                .usageCount(0)
                .tags("")
                .difficultyLevel("intermediate")
                .build();

        Skill saved = skillRepository.save(skill);
        log.info("Nueva skill creada: {} (ID: {})", saved.getName(), saved.getId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<SkillDTO> getAllSkills(Pageable pageable) {
        return skillRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public SkillDTO getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", id));
        return toDTO(skill);
    }

    @Transactional(readOnly = true)
    public Page<SkillDTO> getSkillsByCategory(String category, Pageable pageable) {
        return skillRepository.findByCategory(category, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<SkillDTO> getSkillsByTag(String tag, Pageable pageable) {
        return skillRepository.findByTagsContaining(tag, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getPopularSkills() {
        return skillRepository.findTop10ByOrderByUsageCountDesc()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public void deleteSkill(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill", "id", id);
        }
        skillRepository.deleteById(id);
        log.info("Skill eliminida: id={}", id);
    }

    /**
     * Genera un prompt personalizado a partir de una skill y parámetros.
     */
    @Transactional
    public String generatePrompt(Long skillId, GeneratePromptRequest request) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", "id", skillId));

        String template = skill.getPromptTemplate();

        // Reemplazar placeholders con valores (soporta tanto {PARAM} como {{PARAM}})
        for (Map.Entry<String, Object> entry : request.getParameters().entrySet()) {
            String value = entry.getValue().toString();
            template = template.replace("{{" + entry.getKey() + "}}", value);
            template = template.replace("{" + entry.getKey() + "}", value);
        }

        // Incrementar contador
        skill.setUsageCount(skill.getUsageCount() + 1);
        skillRepository.save(skill);

        log.info("Prompt generado desde skill '{}' con {} parámetros", skill.getName(), request.getParameters().size());

        return template;
    }

    private List<String> parseParameters(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            String cleaned = json.replace("\\\"", "\"");
            List<SkillDTO.SkillParameter> params = objectMapper.readValue(cleaned,
                    new TypeReference<List<SkillDTO.SkillParameter>>() {
                    });
            return params.stream().map(p -> p.getName()).toList();
        } catch (Exception e) {
            log.error("Error parseando parametros: {}", e.getMessage());
            return List.of();
        }
    }

    private SkillDTO toDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .description(skill.getDescription())
                .promptTemplate(skill.getPromptTemplate())
                .parameters(parseParameters(skill.getParameters()))
                .exampleOutput(skill.getExampleOutput())
                .tags(skill.getTags() != null && !skill.getTags().isEmpty() ? Arrays.asList(skill.getTags().split(",")) : List.of())
                .usageCount(skill.getUsageCount())
                .difficultyLevel(skill.getDifficultyLevel())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }
}
