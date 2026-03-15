package com.promptvault.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class GlobalStatsDTO {
    private long totalPrompts;
    private long totalImprovements;
    private long totalMcpServers;
    private long totalSkills;
    private long totalWorkflows;
    private long totalContextPacks;
    private double improvementRatio; // 0.0 - 1.0 (porcentaje de prompts mejorados)
}
