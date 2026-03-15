package com.promptvault.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.MCPServerCreateRequest;
import com.promptvault.dto.MCPServerDTO;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.MCPServer;
import com.promptvault.repository.MCPServerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar servidores MCP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MCPServerService {
    
    private final MCPServerRepository mcpServerRepository;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public MCPServerDTO createMCPServer(MCPServerCreateRequest request) {
        log.info("Creando nuevo MCP server: {}", request.getName());
        
        MCPServer mcpServer = MCPServer.builder()
            .name(request.getName())
            .description(request.getDescription())
            .category(request.getCategory())
            .tags(request.getTags() != null && !request.getTags().isEmpty() ? "," + String.join(",", request.getTags()) + "," : "")
            .command(request.getCommand())
            .args(toJsonString(request.getArgs()))
            .envVars(toJsonString(request.getEnvVars()))
            .capabilities(toJsonString(request.getCapabilities()))
            .documentation(request.getDocumentation())
            .officialUrl(request.getOfficialUrl())
            .installationInstructions(request.getInstallationInstructions())
            .configExample(request.getConfigExample())
            .usageCount(0)
            .verified(false)
            .build();
        
        MCPServer saved = mcpServerRepository.save(mcpServer);
        log.info("MCP server creado con ID: {}", saved.getId());
        
        return toDTO(saved);
    }
    
    @Transactional(readOnly = true)
    public Page<MCPServerDTO> getAllMCPServers(Pageable pageable) {
        return mcpServerRepository.findAll(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public MCPServerDTO getMCPServerById(Long id) {
        MCPServer mcpServer = mcpServerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MCP Server", "id", id));
        return toDTO(mcpServer);
    }
    
    @Transactional(readOnly = true)
    public Page<MCPServerDTO> getMCPServersByCategory(String category, Pageable pageable) {
        return mcpServerRepository.findByCategory(category, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<MCPServerDTO> getMCPServersByTag(String tag, Pageable pageable) {
        return mcpServerRepository.findServersByTag(tag, pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<MCPServerDTO> searchMCPServers(String searchTerm, Pageable pageable) {
        return mcpServerRepository.searchByNameOrDescription(searchTerm, pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public Page<MCPServerDTO> getVerifiedMCPServers(Pageable pageable) {
        return mcpServerRepository.findByVerifiedTrue(pageable).map(this::toDTO);
    }
    
    @Transactional(readOnly = true)
    public List<MCPServerDTO> getPopularMCPServers() {
        return mcpServerRepository.findTop10ByOrderByUsageCountDesc()
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    @Transactional
    public void deleteMCPServer(Long id) {
        if (!mcpServerRepository.existsById(id)) {
            throw new ResourceNotFoundException("MCP Server", "id", id);
        }
        mcpServerRepository.deleteById(id);
        log.info("MCP server eliminado: {}", id);
    }
    
    @Transactional
    public void incrementUsageCount(Long id) {
        MCPServer mcpServer = mcpServerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MCP Server", "id", id));
        mcpServer.setUsageCount(mcpServer.getUsageCount() + 1);
        mcpServerRepository.save(mcpServer);
    }

    // NEW METHOD: generateConfig
    public String generateConfig(List<Long> serverIds, Map<String, Map<String, String>> userEnvVars) {
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        Map<String, Object> servers = new java.util.LinkedHashMap<>();
        
        for (Long id : serverIds) {
            MCPServer server = mcpServerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCP Server", "id", id));
            
            String serverKey = server.getName().toLowerCase().replace(" ", "-");
            Map<String, Object> serverConfig = new java.util.LinkedHashMap<>();
            serverConfig.put("command", server.getCommand());
            serverConfig.put("args", parseJsonList(server.getArgs()));
            
            // 1. Obtener variables por defecto de la base de datos
            Map<String, String> mergedEnvVars = parseJsonMap(server.getEnvVars());
            
            // 2. Sobrescribir con variables proporcionadas por el usuario si existen
            if (userEnvVars != null) {
                // Intentar encontrar variables para este servidor por varias claves
                Map<String, String> userVars = userEnvVars.get(serverKey);
                if (userVars == null || userVars.isEmpty()) {
                    userVars = userEnvVars.get(server.getName().toLowerCase());
                }
                if (userVars == null || userVars.isEmpty()) {
                    userVars = userEnvVars.get(server.getName());
                }
                
                // Solo mezclar si el usuario proporcionó variables no vacías
                if (userVars != null && !userVars.isEmpty()) {
                    mergedEnvVars.putAll(userVars);
                }
            }
            
            if (!mergedEnvVars.isEmpty()) {
                serverConfig.put("env", mergedEnvVars);
            }
            
            servers.put(serverKey, serverConfig);
            
            // Incrementar uso
            incrementUsageCount(id);
        }
        
        config.put("mcpServers", servers);
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            log.error("Error generando JSON de configuración", e);
            throw new RuntimeException("Error generando configuración", e);
        }
    }
    
    /**
     * Convierte objeto a JSON string.
     */
    private String toJsonString(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error convirtiendo objeto a JSON", e);
            return null;
        }
    }

    /**
     * Parsea JSON string a mapa (siempre devuelve un mapa mutable).
     */
    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.trim().isEmpty()) return new java.util.HashMap<>();
        
        String cleaned = cleanJsonString(json);
        if (cleaned == null || cleaned.isEmpty() || cleaned.equals("{}")) return new java.util.HashMap<>();
        
        try {
            Map<String, String> result = objectMapper.readValue(cleaned, new TypeReference<Map<String, String>>() {});
            Map<String, String> mutableMap = new java.util.HashMap<>();
            if (result != null) {
                result.forEach((k, v) -> {
                    if (k != null && v != null) mutableMap.put(k, v);
                });
            }
            return mutableMap;
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON map. Raw: [{}], Cleaned: [{}]. Error: {}", json, cleaned, e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    /**
     * Parsea JSON string a lista (siempre devuelve una lista mutable).
     */
    private List<String> parseJsonList(String json) {
        if (json == null || json.trim().isEmpty()) return new java.util.ArrayList<>();
        
        String cleaned = cleanJsonString(json);
        if (cleaned == null || cleaned.isEmpty() || cleaned.equals("[]")) return new java.util.ArrayList<>();
        
        try {
            List<String> result = objectMapper.readValue(cleaned, new TypeReference<List<String>>() {});
            return result != null ? new java.util.ArrayList<>(result) : new java.util.ArrayList<>();
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON list. Raw: [{}], Cleaned: [{}]. Error: {}", json, cleaned, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Limpia cadenas JSON que pueden venir con escapes o comillas extra de la BD.
     */
    private String cleanJsonString(String json) {
        if (json == null) return null;
        String s = json.trim();
        
        // 1. Eliminar escapes de comillas primero
        if (s.contains("\\\"")) {
            s = s.replace("\\\"", "\"");
        }
        
        // 2. Eliminar comillas envolventes si existen
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() > 2) {
            String inner = s.substring(1, s.length() - 1).trim();
            if (inner.startsWith("{") || inner.startsWith("[") || inner.contains(":")) {
                s = inner;
            }
        }
        
        // 3. Verificación final de integridad
        s = s.trim();
        if (s.isEmpty()) return null;
        
        return s;
    }
    
    /**
     * Convierte entidad a DTO.
     */
    private MCPServerDTO toDTO(MCPServer mcpServer) {
        String configJson = generateConfigJson(mcpServer);
        
        MCPServerDTO dto = new MCPServerDTO();
        dto.setId(mcpServer.getId());
        dto.setName(mcpServer.getName());
        dto.setDescription(mcpServer.getDescription());
        dto.setCategory(mcpServer.getCategory());
        dto.setTags(mcpServer.getTags() != null && !mcpServer.getTags().isEmpty() ? Arrays.asList(mcpServer.getTags().split(",")) : List.of());
        dto.setCommand(mcpServer.getCommand());
        dto.setArgs(parseJsonList(mcpServer.getArgs()));
        dto.setEnvVars(parseJsonMap(mcpServer.getEnvVars()));
        dto.setCapabilities(parseJsonList(mcpServer.getCapabilities()));
        dto.setDocumentation(mcpServer.getDocumentation());
        dto.setOfficialUrl(mcpServer.getOfficialUrl());
        dto.setInstallationInstructions(mcpServer.getInstallationInstructions());
        dto.setConfigExample(mcpServer.getConfigExample());
        dto.setConfigJson(configJson);
        dto.setUsageCount(mcpServer.getUsageCount());
        dto.setRating(mcpServer.getRating());
        dto.setVerified(mcpServer.getVerified());
        dto.setCreatedAt(mcpServer.getCreatedAt());
        dto.setUpdatedAt(mcpServer.getUpdatedAt());
        
        return dto;
    }

    /**
     * Genera el JSON de configuración para un servidor MCP mostrando las claves
     * de las variables de entorno en lugar de los valores.
     */
    private String generateConfigJson(MCPServer server) {
        log.info("generateConfigJson - Server: {}, Raw envVars: [{}]", server.getName(), server.getEnvVars());
        
        Map<String, Object> config = new java.util.LinkedHashMap<>();
        Map<String, Object> servers = new java.util.LinkedHashMap<>();

        String serverKey = server.getName().toLowerCase().replace(" ", "-");
        Map<String, Object> serverConfig = new java.util.LinkedHashMap<>();
        serverConfig.put("command", server.getCommand());
        serverConfig.put("args", parseJsonList(server.getArgs()));

        // Obtener las claves de las variables de entorno desde envVars
        Map<String, String> envVarsMap = parseJsonMap(server.getEnvVars());
        log.info("generateConfigJson - Parsed envVarsMap: {}", envVarsMap);
        
        if (!envVarsMap.isEmpty()) {
            // Crear un mapa con las claves y valores placeholder
            Map<String, String> envTemplate = new java.util.LinkedHashMap<>();
            for (String key : envVarsMap.keySet()) {
                // Usar el valor original si existe, sino usar placeholder
                String value = envVarsMap.get(key);
                if (value == null || value.trim().isEmpty() || (value.startsWith("<") && value.endsWith(">"))) {
                    envTemplate.put(key, "<" + key + "_VALUE>");
                } else {
                    // Mantener valor placeholder si parece un placeholder
                    envTemplate.put(key, value);
                }
            }
            serverConfig.put("env", envTemplate);
        }

        servers.put(serverKey, serverConfig);
        config.put("mcpServers", servers);

        try {
            String result = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            log.info("generateConfigJson - Result: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error generando configJson", e);
            return null;
        }
    }

    /**
     * Genera una plantilla de configuracion para un servidor MCP mostrando las claves
     * de las variables de entorno definidas en envVars.
     *
     * @param serverId el ID del servidor MCP
     * @return JSON de configuracion con las claves de variables de entorno
     */
    public String getConfigTemplate(Long serverId) {
        MCPServer server = mcpServerRepository.findById(serverId)
            .orElseThrow(() -> new ResourceNotFoundException("MCP Server", "id", serverId));

        log.info("getConfigTemplate - Server ID: {}", serverId);
        log.info("getConfigTemplate - Raw envVars from DB: [{}]", server.getEnvVars());

        Map<String, Object> config = new java.util.LinkedHashMap<>();
        Map<String, Object> servers = new java.util.LinkedHashMap<>();

        String serverKey = server.getName().toLowerCase().replace(" ", "-");
        Map<String, Object> serverConfig = new java.util.LinkedHashMap<>();
        serverConfig.put("command", server.getCommand());
        serverConfig.put("args", parseJsonList(server.getArgs()));

        // Obtener las claves de las variables de entorno desde envVars
        Map<String, String> envVarsMap = parseJsonMap(server.getEnvVars());
        log.info("getConfigTemplate - Parsed envVarsMap: {}", envVarsMap);
        
        if (!envVarsMap.isEmpty()) {
            // Crear un mapa con las claves y valores placeholder
            Map<String, String> envTemplate = new java.util.LinkedHashMap<>();
            for (String key : envVarsMap.keySet()) {
                envTemplate.put(key, "<" + key + "_VALUE>");
            }
            serverConfig.put("env", envTemplate);
        }

        servers.put(serverKey, serverConfig);
        config.put("mcpServers", servers);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            log.error("Error generando JSON de plantilla de configuracion", e);
            throw new RuntimeException("Error generando plantilla de configuracion", e);
        }
    }
}
