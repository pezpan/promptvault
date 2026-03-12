package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptImprovementDTO {
    private Long id;
    private String originalContent;
    private String improvedContent;
    private List<String> improvements;
    private String quality;
    private Integer completeness;
    private LocalDateTime createdAt;
}
