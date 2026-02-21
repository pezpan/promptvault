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
public class ContextPackDetailDTO {
    private Long id;
    private String name;
    private String description;
    private String emoji;
    private String category;
    private String setupInstructions;
    private List<String> tags;
    private int usageCount;

    // Recursos completos
    private List<PromptDTO> prompts;
    private List<SkillDTO> skills;
    private List<MCPServerDTO> mcpServers;

    // Config JSON lista para copiar (generada automáticamente)
    private String generatedMcpConfig;
}
