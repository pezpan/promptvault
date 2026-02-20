package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillBuildResult {
    // La skill generada (puede guardarse directamente con saveToDatabase=true)
    private GeneratedSkill skill;

    // Metadata del proceso de generación
    private String modelUsed;
    private Integer tokensUsed;
    private Long generationTimeMs;

    // Si saveToDatabase=true, el ID asignado
    private Long savedSkillId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedSkill {
        private String name;
        private String description;
        private String template;          // El template con {{parámetros}}
        private List<String> parameters;  // Lista de parámetros detectados en el template
        private Map<String, String> parameterDescriptions; // Descripción de cada parámetro
        private String exampleOutput;     // Ejemplo de output generado por la IA
        private String category;
        private int estimatedQualityScore; // 0-100
    }
}
