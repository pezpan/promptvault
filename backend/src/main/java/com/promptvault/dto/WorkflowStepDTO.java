package com.promptvault.dto;

import com.promptvault.model.WorkflowStep.StepType;
import com.promptvault.model.WorkflowStep.TransformType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowStepDTO {
    private Long id;
    private int stepOrder;
    private String name;
    private StepType stepType;

    // Para SKILL
    private Long skillId;
    private String skillName;           // Nombre de la skill (resuelto)
    private Map<String, String> skillParameters;

    // Para FREE_PROMPT
    private String promptTemplate;

    // Para TRANSFORM
    private TransformType transformType;

    // Común
    private String systemInstruction;
}
