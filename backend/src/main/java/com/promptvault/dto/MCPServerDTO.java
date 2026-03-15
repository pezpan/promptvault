package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para representar un servidor MCP en las respuestas de la API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class MCPServerDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private List<String> tags;
    private String command;
    private List<String> args;
    private Map<String, String> envVars;
    private List<String> capabilities;
    private String documentation;
    private String officialUrl;
    private String installationInstructions;
    private String configExample;
    private String configJson;  // Configuración generada dinámicamente con las claves de envVars
    private Integer usageCount;
    private Double rating;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
