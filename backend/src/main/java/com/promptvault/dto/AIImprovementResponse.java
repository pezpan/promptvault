package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que contiene el resultado de mejorar un prompt con IA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIImprovementResponse {
    
    /**
     * Contenido original del prompt.
     */
    private String originalContent;
    
    /**
     * Contenido mejorado por la IA.
     */
    private String improvedContent;
    
    /**
     * Lista de mejoras realizadas (explicación).
     */
    private List<String> improvements;
    
    /**
     * Validación de estructura (indica qué secciones están presentes).
     */
    private StructureValidation structureValidation;
    
    /**
     * Tokens utilizados en la petición (opcional).
     */
    private TokenUsage tokenUsage;
    
    /**
     * Información sobre qué secciones del prompt están presentes.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StructureValidation {
        private boolean hasRol;
        private boolean hasTarea;
        private boolean hasAudiencia;
        private boolean hasFormato;
        private boolean hasContexto;
        private int completeness; // Porcentaje (0-100)
        private String quality; // "Excelente", "Buena", "Mejorable"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
