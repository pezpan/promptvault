package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class DashboardDTO {
    private GlobalStatsDTO global;
    private PromptStatsDTO prompts;
    private AIStatsDTO ai;
    private LocalDateTime generatedAt;
}
