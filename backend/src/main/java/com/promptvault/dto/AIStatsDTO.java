package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AIStatsDTO {
    private String mostUsedSkill;
    private String mostPopularMcpServer;
    private long totalAICallsMade;
    private List<RecentImprovementDTO> recentImprovements;
}
