package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowExecutionResult {
    private Long workflowId;
    private String workflowName;
    private boolean success;

    // El output final (último paso)
    private String finalOutput;

    // Outputs intermedios de cada paso
    private List<StepResult> stepResults;

    // Métricas de ejecución
    private int totalSteps;
    private int completedSteps;
    private long totalExecutionTimeMs;
    private int totalTokensUsed; // estimado

    private LocalDateTime executedAt;
    private String errorMessage; // Solo si success = false

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepResult {
        private int stepOrder;
        private String stepName;
        private String stepType;    // SKILL / FREE_PROMPT / TRANSFORM
        private String input;       // El prompt completo enviado a Groq (con contexto acumulado)
        private String output;      // La respuesta de Groq
        private long executionTimeMs;
        private boolean success;
        private String errorMessage;
    }
}
