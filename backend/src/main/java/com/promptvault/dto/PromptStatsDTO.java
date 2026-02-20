package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class PromptStatsDTO {
    private Map<String, Long> promptsByCategory;
    private long improvedPrompts;
    private long notImprovedPrompts;
}
