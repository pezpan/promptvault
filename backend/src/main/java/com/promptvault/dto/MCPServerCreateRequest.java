package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO para crear un nuevo servidor MCP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPServerCreateRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    private String description;
    
    @NotBlank(message = "La categoría es obligatoria")
    private String category;
    
    private List<String> tags;
    
    @NotBlank(message = "El comando es obligatorio")
    private String command;
    
    private List<String> args;
    
    private Map<String, String> envVars;
    
    private List<String> capabilities;
    
    private String documentation;
    
    private String officialUrl;
    
    private String installationInstructions;
    
    private String configExample;
}
