package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextPackCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private String emoji;
    private String category;
    private List<Long> promptIds;
    private List<Long> skillIds;
    private List<Long> mcpServerIds;
    private String setupInstructions;
    private List<String> tags;
}
