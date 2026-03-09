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
public class PromptDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private List<String> tags;
    private String project;
    private Boolean isFavorite;
    private Integer usageCount;
    private String status;
    private Integer qualityScore;
    private LocalDateTime lastImprovedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}