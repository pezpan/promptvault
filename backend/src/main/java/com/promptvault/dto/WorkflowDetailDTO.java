package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String inputDescription;
    private String outputDescription;
    private int usageCount;
    private List<WorkflowStepDTO> steps;
}
