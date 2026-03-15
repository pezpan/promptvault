package com.promptvault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.*;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.ContextPack;
import com.promptvault.model.MCPServer;
import com.promptvault.model.Prompt;
import com.promptvault.model.Skill;
import com.promptvault.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContextPackService {

    private final ContextPackRepository contextPackRepository;
    private final PromptRepository promptRepository;
    private final SkillRepository skillRepository;
    private final MCPServerRepository mcpServerRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ContextPackDTO> findAll() {
        return contextPackRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContextPackDetailDTO findById(Long id) {
        ContextPack pack = contextPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContextPack", "id", id));
        pack.setUsageCount(pack.getUsageCount() + 1);
        contextPackRepository.save(pack);
        return toDetailDTO(pack);
    }

    @Transactional(readOnly = true)
    public List<ContextPackDTO> findByCategory(String category) {
        return contextPackRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ContextPackDTO> findPopular() {
        return contextPackRepository.findTop5ByOrderByUsageCountDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ContextPackDetailDTO create(ContextPackCreateRequest req) {
        ContextPack pack = ContextPack.builder()
                .name(req.getName())
                .description(req.getDescription())
                .emoji(req.getEmoji() != null ? req.getEmoji() : "package-box")
                .category(req.getCategory())
                .promptIds(req.getPromptIds() != null ? req.getPromptIds() : List.of())
                .skillIds(req.getSkillIds() != null ? req.getSkillIds() : List.of())
                .mcpServerIds(req.getMcpServerIds() != null ? req.getMcpServerIds() : List.of())
                .setupInstructions(req.getSetupInstructions())
                .tags(req.getTags() != null ? req.getTags() : List.of())
                .build();
        return toDetailDTO(contextPackRepository.save(pack));
    }

    @Transactional
    public void delete(Long id) {
        if (!contextPackRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContextPack", "id", id);
        }
        contextPackRepository.deleteById(id);
    }

    private ContextPackDTO toDTO(ContextPack pack) {
        // Cargar MCP servers para generar la config
        List<MCPServerDTO> mcps = pack.getMcpServerIds() != null && !pack.getMcpServerIds().isEmpty()
                ? mcpServerRepository.findAllById(pack.getMcpServerIds()).stream()
                    .map(this::mcpServerToDTO)
                    .collect(Collectors.toList())
                : List.of();
        
        // Generar config JSON de todos los MCPs del pack
        String mcpConfig = generateMcpConfigForPack(mcps);
        
        return ContextPackDTO.builder()
                .id(pack.getId())
                .name(pack.getName())
                .description(pack.getDescription())
                .emoji(pack.getEmoji())
                .category(pack.getCategory())
                .usageCount(pack.getUsageCount())
                .tags(pack.getTags())
                .promptCount(pack.getPromptIds() != null ? pack.getPromptIds().size() : 0)
                .skillCount(pack.getSkillIds() != null ? pack.getSkillIds().size() : 0)
                .mcpServerCount(pack.getMcpServerIds() != null ? pack.getMcpServerIds().size() : 0)
                .generatedMcpConfig(mcpConfig)
                .build();
    }

    private ContextPackDetailDTO toDetailDTO(ContextPack pack) {
        // Cargar recursos reales desde BD
        List<PromptDTO> prompts = pack.getPromptIds() != null && !pack.getPromptIds().isEmpty()
                ? promptRepository.findAllById(pack.getPromptIds()).stream()
                    .map(this::promptToDTO)
                    .collect(Collectors.toList())
                : List.of();

        List<SkillDTO> skills = pack.getSkillIds() != null && !pack.getSkillIds().isEmpty()
                ? skillRepository.findAllById(pack.getSkillIds()).stream()
                    .map(this::skillToDTO)
                    .collect(Collectors.toList())
                : List.of();

        List<MCPServerDTO> mcps = pack.getMcpServerIds() != null && !pack.getMcpServerIds().isEmpty()
                ? mcpServerRepository.findAllById(pack.getMcpServerIds()).stream()
                    .map(this::mcpServerToDTO)
                    .collect(Collectors.toList())
                : List.of();

        // Generar config JSON de todos los MCPs del pack
        String mcpConfig = generateMcpConfigForPack(mcps);

        return ContextPackDetailDTO.builder()
                .id(pack.getId())
                .name(pack.getName())
                .description(pack.getDescription())
                .emoji(pack.getEmoji())
                .category(pack.getCategory())
                .setupInstructions(pack.getSetupInstructions())
                .tags(pack.getTags())
                .usageCount(pack.getUsageCount())
                .prompts(prompts)
                .skills(skills)
                .mcpServers(mcps)
                .generatedMcpConfig(mcpConfig)
                .build();
    }

    private PromptDTO promptToDTO(Prompt p) {
        return PromptDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .content(p.getContent())
                .category(p.getCategory())
                .tags(p.getTags() != null ? Arrays.asList(p.getTags().split(",")) : List.of())
                .isFavorite(p.getIsFavorite())
                .usageCount(p.getUsageCount())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private SkillDTO skillToDTO(Skill s) {
        return SkillDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .category(s.getCategory())
                .description(s.getDescription())
                .promptTemplate(s.getPromptTemplate())
                .exampleOutput(s.getExampleOutput())
                .usageCount(s.getUsageCount())
                .difficultyLevel(s.getDifficultyLevel())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    private MCPServerDTO mcpServerToDTO(MCPServer m) {
        return MCPServerDTO.builder()
                .id(m.getId())
                .name(m.getName())
                .description(m.getDescription())
                .category(m.getCategory())
                .command(m.getCommand())
                .args(parseJsonList(m.getArgs()))
                .envVars(parseJsonMap(m.getEnvVars()))
                .officialUrl(m.getOfficialUrl())
                .verified(m.getVerified())
                .usageCount(m.getUsageCount())
                .build();
    }

    private String generateMcpConfigForPack(List<MCPServerDTO> mcps) {
        if (mcps.isEmpty()) return "{}";
        
        // Genera un JSON de config combinado para todos los MCPs del pack
        Map<String, Object> config = new LinkedHashMap<>();
        Map<String, Object> servers = new LinkedHashMap<>();

        for (MCPServerDTO mcp : mcps) {
            String key = mcp.getName().toLowerCase().replace(" ", "-");
            Map<String, Object> serverConfig = new LinkedHashMap<>();
            serverConfig.put("command", mcp.getCommand() != null ? mcp.getCommand() : "npx");

            // Usar args reales o por defecto
            List<String> args = mcp.getArgs() != null && !mcp.getArgs().isEmpty() 
                ? mcp.getArgs() 
                : List.of("-y", "@modelcontextprotocol/server-" + key);
            serverConfig.put("args", args);
            
            // Agregar env vars con placeholders para mostrar las claves requeridas
            if (mcp.getEnvVars() != null && !mcp.getEnvVars().isEmpty()) {
                Map<String, String> envTemplate = new LinkedHashMap<>();
                for (String envKey : mcp.getEnvVars().keySet()) {
                    envTemplate.put(envKey, "<" + envKey + "_VALUE>");
                }
                serverConfig.put("env", envTemplate);
            }

            servers.put(key, serverConfig);
        }

        config.put("mcpServers", servers);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * Parsea JSON string a lista de strings.
     */
    private List<String> parseJsonList(String json) {
        if (json == null || json.trim().isEmpty()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * Parsea JSON string a mapa de strings.
     */
    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.trim().isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
