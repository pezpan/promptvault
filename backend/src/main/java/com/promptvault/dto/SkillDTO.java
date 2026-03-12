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
public class SkillDTO {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String promptTemplate;
    private List<String> parameters;
    private String exampleOutput;
    private List<String> tags;
    private Integer usageCount;
    private String difficultyLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillParameter {
        private String name;
        private String type;  // "text", "select", "multiselect", "number"
        private String description;
        private List<String> options;  // Para select/multiselect
        private String defaultValue;
        private Boolean required;
    }
}
