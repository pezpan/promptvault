package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GlobalStatsDTO {
    private long totalPrompts;
    private long totalImprovements;
    private long totalMcpServers;
    private long totalSkills;
    private double improvementRatio; // 0.0 - 1.0 (porcentaje de prompts mejorados)
}
