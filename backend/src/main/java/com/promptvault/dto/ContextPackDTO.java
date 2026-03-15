package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextPackDTO {
    private Long id;
    private String name;
    private String description;
    private String emoji;
    private String category;
    private int usageCount;
    private List<String> tags;

    // Recursos incluidos (resumen, no los objetos completos)
    private int promptCount;
    private int skillCount;
    private int mcpServerCount;
    
    // Configuración MCP generada para el pack
    private String generatedMcpConfig;
}
