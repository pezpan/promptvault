package com.promptvault.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptUpdateRequest {
    
    @Size(max = 255)
    private String title;
    
    @Size(max = 500)
    private String description;
    
    private String content;
    
    private String category;
    
    private List<String> tags;
    
    private String project;
    
    private Boolean isFavorite;
}
