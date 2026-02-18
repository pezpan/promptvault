# ISSUES 28-31: Endpoints REST y Seed Data para MCP Servers

## ISSUE 28: Controller REST para MCP Servers

**Archivo**: `backend/src/main/java/com/promptvault/controller/MCPServerController.java`

```java
package com.promptvault.controller;

import com.promptvault.dto.MCPServerCreateRequest;
import com.promptvault.dto.MCPServerDTO;
import com.promptvault.service.MCPServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp-servers")
@RequiredArgsConstructor
@Tag(name = "MCP Servers", description = "API para gestión de servidores MCP")
public class MCPServerController {
    
    private final MCPServerService mcpServerService;
    
    @PostMapping
    @Operation(summary = "Crear un nuevo servidor MCP")
    public ResponseEntity<MCPServerDTO> createMCPServer(@Valid @RequestBody MCPServerCreateRequest request) {
        MCPServerDTO created = mcpServerService.createMCPServer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Listar todos los servidores MCP")
    public ResponseEntity<Page<MCPServerDTO>> getAllMCPServers(
        @PageableDefault(size = 20, sort = "usageCount", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Boolean verified
    ) {
        Page<MCPServerDTO> servers;
        
        if (search != null && !search.isBlank()) {
            servers = mcpServerService.searchMCPServers(search, pageable);
        } else if (category != null && !category.isBlank()) {
            servers = mcpServerService.getMCPServersByCategory(category, pageable);
        } else if (Boolean.TRUE.equals(verified)) {
            servers = mcpServerService.getVerifiedMCPServers(pageable);
        } else {
            servers = mcpServerService.getAllMCPServers(pageable);
        }
        
        return ResponseEntity.ok(servers);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un servidor MCP por ID")
    public ResponseEntity<MCPServerDTO> getMCPServerById(@PathVariable Long id) {
        MCPServerDTO server = mcpServerService.getMCPServerById(id);
        return ResponseEntity.ok(server);
    }
    
    @GetMapping("/popular")
    @Operation(summary = "Obtener los servidores MCP más populares")
    public ResponseEntity<List<MCPServerDTO>> getPopularMCPServers() {
        List<MCPServerDTO> popular = mcpServerService.getPopularMCPServers();
        return ResponseEntity.ok(popular);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un servidor MCP")
    public ResponseEntity<Void> deleteMCPServer(@PathVariable Long id) {
        mcpServerService.deleteMCPServer(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/increment-usage")
    @Operation(summary = "Incrementar contador de uso")
    public ResponseEntity<Void> incrementUsage(@PathVariable Long id) {
        mcpServerService.incrementUsageCount(id);
        return ResponseEntity.ok().build();
    }
}
```

---

## ISSUE 29: Seed Data - 10 MCP Servers Populares

**Archivo**: `backend/src/main/resources/mcp-servers-data.sql`

```sql
-- Seed data para MCP Servers populares
INSERT INTO mcp_servers (name, description, category, tags, command, args, env_vars, capabilities, documentation, official_url, installation_instructions, config_example, usage_count, verified) VALUES

-- 1. GitHub MCP Server
('GitHub', 'Acceso completo a repositorios, issues, PRs y búsqueda de código en GitHub', 'development', ARRAY['github', 'git', 'code', 'repositories'], 'npx', '[["-y", "@modelcontextprotocol/server-github"]]', '{"GITHUB_TOKEN": "<your_github_token>"}', '["read_repos", "create_issues", "search_code", "manage_prs"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github', 'Requiere Node.js instalado. Generar token en GitHub Settings > Developer settings > Personal access tokens', '{"mcpServers": {"github": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-github"], "env": {"GITHUB_TOKEN": "ghp_xxx"}}}}', 0, true),

-- 2. Filesystem MCP Server
('Filesystem', 'Leer y escribir archivos en el sistema local', 'filesystem', ARRAY['files', 'local', 'storage'], 'npx', '[["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/directory"]]', '{}', '["read_file", "write_file", "list_directory", "create_directory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem', 'Especifica el directorio permitido como argumento. Solo puede acceder a ese directorio y subdirectorios', '{"mcpServers": {"filesystem": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/username/Documents"]}}}', 0, true),

-- 3. PostgreSQL MCP Server
('PostgreSQL', 'Ejecutar consultas SQL en bases de datos PostgreSQL', 'database', ARRAY['postgresql', 'sql', 'database'], 'npx', '[["-y", "@modelcontextprotocol/server-postgres"]]', '{"POSTGRES_URL": "postgresql://user:pass@localhost:5432/db"}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/postgres', 'Requiere PostgreSQL instalado y accesible. Proporciona URL de conexión', '{"mcpServers": {"postgres": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-postgres"], "env": {"POSTGRES_URL": "postgresql://localhost/mydb"}}}}', 0, true),

-- 4. Slack MCP Server
('Slack', 'Enviar mensajes, leer canales y gestionar workspace de Slack', 'productivity', ARRAY['slack', 'messaging', 'collaboration'], 'npx', '[["-y", "@modelcontextprotocol/server-slack"]]', '{"SLACK_BOT_TOKEN": "xoxb-your-token", "SLACK_TEAM_ID": "T01234"}', '["send_message", "read_channel", "list_channels", "user_info"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/slack', 'Crear Slack App en api.slack.com con permisos necesarios. Instalar en workspace y copiar Bot Token', '{"mcpServers": {"slack": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-slack"], "env": {"SLACK_BOT_TOKEN": "xoxb-xxx", "SLACK_TEAM_ID": "T01234"}}}}', 0, true),

-- 5. Google Drive MCP Server
('Google Drive', 'Buscar, leer y gestionar archivos en Google Drive', 'productivity', ARRAY['google-drive', 'cloud-storage', 'files'], 'npx', '[["-y", "@modelcontextprotocol/server-gdrive"]]', '{"GDRIVE_CLIENT_ID": "xxx", "GDRIVE_CLIENT_SECRET": "xxx", "GDRIVE_REFRESH_TOKEN": "xxx"}', '["search_files", "read_file", "create_file", "share_file"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/gdrive', 'Crear proyecto en Google Cloud Console, habilitar Drive API, crear credenciales OAuth', '{"mcpServers": {"gdrive": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-gdrive"], "env": {"GDRIVE_CLIENT_ID": "xxx"}}}}', 0, true),

-- 6. Brave Search MCP Server
('Brave Search', 'Búsqueda web usando Brave Search API', 'search', ARRAY['search', 'web', 'brave'], 'npx', '[["-y", "@modelcontextprotocol/server-brave-search"]]', '{"BRAVE_API_KEY": "<your_api_key>"}', '["web_search", "local_search", "news_search"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search', 'Obtener API key gratuita en brave.com/search/api', '{"mcpServers": {"brave": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-brave-search"], "env": {"BRAVE_API_KEY": "BSA_xxx"}}}}', 0, true),

-- 7. Memory MCP Server
('Memory', 'Sistema de memoria persistente para recordar información entre sesiones', 'utility', ARRAY['memory', 'storage', 'context'], 'npx', '[["-y", "@modelcontextprotocol/server-memory"]]', '{}', '["store_memory", "retrieve_memory", "search_memory", "delete_memory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/memory', 'No requiere configuración adicional. Almacena información en archivo local', '{"mcpServers": {"memory": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-memory"]}}}', 0, true),

-- 8. Puppeteer MCP Server
('Puppeteer', 'Automatización de navegador web con Puppeteer', 'automation', ARRAY['puppeteer', 'browser', 'scraping', 'automation'], 'npx', '[["-y", "@modelcontextprotocol/server-puppeteer"]]', '{}', '["navigate", "screenshot", "click", "type", "extract_content"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/puppeteer', 'Permite controlar Chrome/Chromium para web scraping y automatización', '{"mcpServers": {"puppeteer": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-puppeteer"]}}}', 0, true),

-- 9. SQLite MCP Server
('SQLite', 'Consultar bases de datos SQLite locales', 'database', ARRAY['sqlite', 'database', 'sql'], 'npx', '[["-y", "@modelcontextprotocol/server-sqlite", "/path/to/database.db"]]', '{}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite', 'Especifica la ruta al archivo .db como argumento', '{"mcpServers": {"sqlite": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-sqlite", "/data/app.db"]}}}', 0, true),

-- 10. Fetch MCP Server
('Fetch', 'Realizar peticiones HTTP/HTTPS para obtener contenido web', 'web', ARRAY['fetch', 'http', 'web', 'api'], 'npx', '[["-y", "@modelcontextprotocol/server-fetch"]]', '{}', '["fetch_url", "fetch_html", "fetch_json"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/fetch', 'Permite obtener contenido de URLs. Útil para leer páginas web y APIs', '{"mcpServers": {"fetch": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-fetch"]}}}', 0, true);
```

**EJECUTAR** al arrancar la aplicación (añadir a import.sql si se usa H2, o ejecutar manualmente en PostgreSQL).

---

## ISSUE 30: Endpoint para Generar Configuración

**Añadir al MCPServerController.java**:

```java
@PostMapping("/generate-config")
@Operation(summary = "Genera archivo de configuración para múltiples servidores MCP")
public ResponseEntity<String> generateConfig(@RequestBody GenerateConfigRequest request) {
    String config = mcpServerService.generateConfig(request.getServerIds(), request.getEnvVars());
    return ResponseEntity.ok()
        .header("Content-Type", "application/json")
        .header("Content-Disposition", "attachment; filename=\"mcp-config.json\"")
        .body(config);
}
```

**DTO**: `GenerateConfigRequest.java`

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateConfigRequest {
    private List<Long> serverIds;
    private Map<String, Map<String, String>> envVars;  // serverName -> {KEY: value}
}
```

**Método en MCPServerService**:

```java
public String generateConfig(List<Long> serverIds, Map<String, Map<String, String>> userEnvVars) {
    Map<String, Object> config = new java.util.LinkedHashMap<>();
    Map<String, Object> servers = new java.util.LinkedHashMap<>();
    
    for (Long id : serverIds) {
        MCPServer server = mcpServerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("MCP Server", "id", id));
        
        Map<String, Object> serverConfig = new java.util.LinkedHashMap<>();
        serverConfig.put("command", server.getCommand());
        serverConfig.put("args", parseJsonList(server.getArgs()));
        
        // Merge env vars
        Map<String, String> envVars = parseJsonMap(server.getEnvVars());
        if (userEnvVars != null && userEnvVars.containsKey(server.getName().toLowerCase())) {
            envVars.putAll(userEnvVars.get(server.getName().toLowerCase()));
        }
        
        if (!envVars.isEmpty()) {
            serverConfig.put("env", envVars);
        }
        
        servers.put(server.getName().toLowerCase().replace(" ", "-"), serverConfig);
        
        // Incrementar uso
        incrementUsageCount(id);
    }
    
    config.put("mcpServers", servers);
    
    try {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Error generando configuración", e);
    }
}
```

---

## ISSUE 31: Categorías de MCP Servers

**Archivo**: `backend/src/main/java/com/promptvault/model/MCPCategory.java`

```java
package com.promptvault.model;

/**
 * Categorías disponibles para servidores MCP.
 */
public enum MCPCategory {
    DEVELOPMENT("development", "Herramientas de desarrollo"),
    DATABASE("database", "Bases de datos"),
    PRODUCTIVITY("productivity", "Productividad y colaboración"),
    SEARCH("search", "Búsqueda web"),
    FILESYSTEM("filesystem", "Sistema de archivos"),
    AUTOMATION("automation", "Automatización"),
    WEB("web", "Acceso web y APIs"),
    UTILITY("utility", "Utilidades");
    
    private final String code;
    private final String displayName;
    
    MCPCategory(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

**Controller para categorías**:

```java
@GetMapping("/categories")
@Operation(summary = "Listar todas las categorías de MCP servers")
public ResponseEntity<List<Map<String, String>>> getCategories() {
    List<Map<String, String>> categories = Arrays.stream(MCPCategory.values())
        .map(cat -> Map.of(
            "code", cat.getCode(),
            "displayName", cat.getDisplayName()
        ))
        .toList();
    return ResponseEntity.ok(categories);
}
```

---

## Verificación Final Issues 28-31

```bash
mvn clean compile
mvn spring-boot:run

# Probar en Swagger:
# - POST /api/mcp-servers (crear)
# - GET /api/mcp-servers (listar - debe haber 10 servidores)
# - GET /api/mcp-servers/popular (top 10)
# - POST /api/mcp-servers/generate-config (generar config)
# - GET /api/mcp-servers/categories (listar categorías)
```

