# ISSUE 27: Crear DTOs y Service para MCP Servers

Crea los DTOs y el servicio de lógica de negocio para gestionar MCP Servers.

## Archivos a Crear

### 1. MCPServerDTO.java

**Ruta**: `backend/src/main/java/com/promptvault/dto/MCPServerDTO.java`

**Contenido**:

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Integer usageCount;
    private Double rating;
    private Boolean verified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### 2. MCPServerCreateRequest.java

**Ruta**: `backend/src/main/java/com/promptvault/dto/MCPServerCreateRequest.java`

**Contenido**:

```java
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
```

---

### 3. MCPServerService.java

**Ruta**: `backend/src/main/java/com/promptvault/service/MCPServerService.java`

**Contenido COMPLETO**:

```java
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
            .tags(request.getTags() != null ? request.getTags().toArray(new String[0]) : new String[0])
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
     * Parsea JSON string a lista.
     */
    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON list", e);
            return List.of();
        }
    }
    
    /**
     * Parsea JSON string a mapa.
     */
    private Map<String, String> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parseando JSON map", e);
            return Map.of();
        }
    }
    
    /**
     * Convierte entidad a DTO.
     */
    private MCPServerDTO toDTO(MCPServer mcpServer) {
        return MCPServerDTO.builder()
            .id(mcpServer.getId())
            .name(mcpServer.getName())
            .description(mcpServer.getDescription())
            .category(mcpServer.getCategory())
            .tags(mcpServer.getTags() != null ? List.of(mcpServer.getTags()) : List.of())
            .command(mcpServer.getCommand())
            .args(parseJsonList(mcpServer.getArgs()))
            .envVars(parseJsonMap(mcpServer.getEnvVars()))
            .capabilities(parseJsonList(mcpServer.getCapabilities()))
            .documentation(mcpServer.getDocumentation())
            .officialUrl(mcpServer.getOfficialUrl())
            .installationInstructions(mcpServer.getInstallationInstructions())
            .configExample(mcpServer.getConfigExample())
            .usageCount(mcpServer.getUsageCount())
            .rating(mcpServer.getRating())
            .verified(mcpServer.getVerified())
            .createdAt(mcpServer.getCreatedAt())
            .updatedAt(mcpServer.getUpdatedAt())
            .build();
    }
}
```

---

## Verificación

```bash
cd backend
mvn clean compile
```

Debe compilar sin errores.

## Notas

- El servicio maneja la conversión entre JSON (almacenado como string) y objetos Java
- `ObjectMapper` (Jackson) se usa para serializar/deserializar JSON
- Los métodos de búsqueda incluyen: por categoría, texto, verificados, populares
- `incrementUsageCount` permite trackear qué servidores son más usados
- Los arrays/maps null se convierten a listas/mapas vacíos para evitar null pointer exceptions
