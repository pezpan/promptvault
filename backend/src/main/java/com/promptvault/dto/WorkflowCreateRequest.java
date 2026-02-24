package com.promptvault.dto;

import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class WorkflowCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;
    private String category;
    private String inputDescription;
    private String outputDescription;

    @NotEmpty(message = "El workflow debe tener al menos un paso")
    private List<StepRequest> steps;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepRequest {
        @NotBlank
        private String name;

        @Builder.Default
        private StepType stepType = StepType.FREE_PROMPT;

        // Para SKILL
        private Long skillId;
        // Parámetros de la skill. Valor especial "__PREVIOUS_OUTPUT__"
        // indica que ese parámetro se rellena con el output del paso anterior
        private Map<String, String> skillParameters;

        // Para FREE_PROMPT
        // Usar {{PREVIOUS_OUTPUT}} para insertar el output del paso anterior
        private String promptTemplate;

        // Para TRANSFORM
        private TransformType transformType;

        // Común: define el ROL del modelo para este paso
        private String systemInstruction;
    }
}
