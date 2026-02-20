# Issue 50: [CONTEXT-PACKS] DTOs, Service y datos de seed

## Archivos a crear

### `src/main/java/com/promptvault/dto/ContextPackDTO.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
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
}
```

### `src/main/java/com/promptvault/dto/ContextPackDetailDTO.java`
DTO completo con todos los recursos expandidos:
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
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
```

### `src/main/java/com/promptvault/dto/ContextPackCreateRequest.java`
```java
package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ContextPackCreateRequest {
    @NotBlank
    private String name;
    private String description;
    private String emoji;
    private String category;
    private List<Long> promptIds;
    private List<Long> skillIds;
    private List<Long> mcpServerIds;
    private String setupInstructions;
    private List<String> tags;
}
```

---

## `src/main/java/com/promptvault/service/ContextPackService.java`

```java
package com.promptvault.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.*;
import com.promptvault.exception.ResourceNotFoundException;
import com.promptvault.model.ContextPack;
import com.promptvault.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public List<ContextPackDTO> findAll() {
        return contextPackRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ContextPackDetailDTO findById(Long id) {
        ContextPack pack = contextPackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContextPack", "id", id));
        pack.setUsageCount(pack.getUsageCount() + 1);
        contextPackRepository.save(pack);
        return toDetailDTO(pack);
    }

    public List<ContextPackDTO> findByCategory(String category) {
        return contextPackRepository.findByCategory(category).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ContextPackDTO> findPopular() {
        return contextPackRepository.findTop5ByOrderByUsageCountDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ContextPackDetailDTO create(ContextPackCreateRequest req) {
        ContextPack pack = new ContextPack();
        pack.setName(req.getName());
        pack.setDescription(req.getDescription());
        pack.setEmoji(req.getEmoji() != null ? req.getEmoji() : "📦");
        pack.setCategory(req.getCategory());
        pack.setPromptIds(req.getPromptIds() != null ? req.getPromptIds() : List.of());
        pack.setSkillIds(req.getSkillIds() != null ? req.getSkillIds() : List.of());
        pack.setMcpServerIds(req.getMcpServerIds() != null ? req.getMcpServerIds() : List.of());
        pack.setSetupInstructions(req.getSetupInstructions());
        pack.setTags(req.getTags() != null ? req.getTags() : List.of());
        return toDetailDTO(contextPackRepository.save(pack));
    }

    public void delete(Long id) {
        if (!contextPackRepository.existsById(id)) {
            throw new ResourceNotFoundException("ContextPack", "id", id);
        }
        contextPackRepository.deleteById(id);
    }

    private ContextPackDTO toDTO(ContextPack pack) {
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
                .build();
    }

    private ContextPackDetailDTO toDetailDTO(ContextPack pack) {
        // Cargar recursos reales desde BD
        List<PromptDTO> prompts = pack.getPromptIds() != null
                ? promptRepository.findAllById(pack.getPromptIds()).stream()
                    .map(p -> PromptDTO.builder()
                        .id(p.getId()).title(p.getTitle()).content(p.getContent()).build())
                    .collect(Collectors.toList())
                : List.of();

        List<SkillDTO> skills = pack.getSkillIds() != null
                ? skillRepository.findAllById(pack.getSkillIds()).stream()
                    .map(s -> SkillDTO.builder()
                        .id(s.getId()).name(s.getName()).description(s.getDescription()).build())
                    .collect(Collectors.toList())
                : List.of();

        List<MCPServerDTO> mcps = pack.getMcpServerIds() != null
                ? mcpServerRepository.findAllById(pack.getMcpServerIds()).stream()
                    .map(m -> MCPServerDTO.builder()
                        .id(m.getId()).name(m.getName()).description(m.getDescription()).build())
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

    private String generateMcpConfigForPack(List<MCPServerDTO> mcps) {
        if (mcps.isEmpty()) return "{}";
        // Genera un JSON de config combinado para todos los MCPs del pack
        StringBuilder sb = new StringBuilder("{\n  \"mcpServers\": {\n");
        for (int i = 0; i < mcps.size(); i++) {
            MCPServerDTO mcp = mcps.get(i);
            String key = mcp.getName().toLowerCase().replace(" ", "-");
            sb.append("    \"").append(key).append("\": {\n");
            sb.append("      \"command\": \"npx\",\n");
            sb.append("      \"args\": [\"-y\", \"@modelcontextprotocol/server-").append(key).append("\"]\n");
            sb.append("    }");
            if (i < mcps.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  }\n}");
        return sb.toString();
    }
}
```

## Nota sobre PromptDTO, SkillDTO, MCPServerDTO
Adaptar la construcción de los DTOs a los campos que realmente existen en cada clase.
Si los DTOs ya tienen un mapper o factory method, usarlo en lugar del builder manual.

---

## Seed data - 4 Context Packs predefinidos

Añadir al arranque de la aplicación (o en un `@Component` con `@PostConstruct` si
el seed via SQL es complejo por las colecciones):

```java
// Pack 1: Security Audit
// prompts: [1] (adaptar IDs reales), skills: [5 = Bug Hunter], mcps: [1=GitHub, 2=Filesystem]

// Pack 2: AI Development
// prompts: [1, 2, 3], skills: [1=Code Reviewer, 3=API Docs, 4=Refactoring], mcps: [2=Filesystem, 10=Fetch]

// Pack 3: Database Development
// prompts: [2], skills: [2=Test Generator], mcps: [3=PostgreSQL, 9=SQLite]

// Pack 4: Content Writing
// prompts: [3], skills: [3=API Documentation Writer], mcps: [6=Brave Search, 5=Google Drive]
```

## Verificación
```bash
mvn clean compile && mvn spring-boot:run
# GET /api/context-packs debe devolver 4 packs
```
