package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RecentImprovementDTO {
    private Long promptId;
    private String promptTitle;
    private LocalDateTime improvedAt;
}
