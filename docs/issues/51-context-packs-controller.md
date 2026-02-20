# Issue 51: [CONTEXT-PACKS] ContextPackController - Endpoints REST

## Archivo a crear
`src/main/java/com/promptvault/controller/ContextPackController.java`

## Implementación completa

```java
package com.promptvault.controller;

import com.promptvault.dto.*;
import com.promptvault.service.ContextPackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/context-packs")
@RequiredArgsConstructor
@Tag(name = "Context Packs", description = "Bundles preconfigurados de prompts, skills y MCP servers por dominio")
public class ContextPackController {

    private final ContextPackService contextPackService;

    @GetMapping
    @Operation(
        summary = "Listar todos los Context Packs",
        description = "Devuelve todos los packs disponibles con un resumen de cuántos recursos incluye cada uno."
    )
    public ResponseEntity<List<ContextPackDTO>> findAll(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(contextPackService.findByCategory(category));
        }
        return ResponseEntity.ok(contextPackService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Ver Context Pack completo",
        description = """
            Devuelve el pack completo con todos sus recursos expandidos:
            - Lista de prompts incluidos
            - Lista de skills incluidas
            - Lista de MCP servers incluidos
            - Config JSON de MCPs lista para copiar a Claude Desktop
            - Instrucciones de setup
            
            **Nota**: Cada llamada incrementa el contador de uso del pack.
            """
    )
    public ResponseEntity<ContextPackDetailDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(contextPackService.findById(id));
    }

    @GetMapping("/popular")
    @Operation(
        summary = "Top 5 Context Packs más usados",
        description = "Ordenados por número de visualizaciones."
    )
    public ResponseEntity<List<ContextPackDTO>> findPopular() {
        return ResponseEntity.ok(contextPackService.findPopular());
    }

    @PostMapping
    @Operation(
        summary = "Crear nuevo Context Pack",
        description = """
            Crea un pack personalizado agrupando recursos existentes por sus IDs.
            
            **Ejemplo:**
            ```json
            {
              "name": "My DevOps Pack",
              "description": "Todo lo necesario para automatización DevOps",
              "emoji": "🚀",
              "category": "devops",
              "promptIds": [1, 2],
              "skillIds": [1, 4],
              "mcpServerIds": [1, 2, 8],
              "setupInstructions": "1. Configurar GITHUB_TOKEN\\n2. Instalar puppeteer",
              "tags": ["devops", "automation", "github"]
            }
            ```
            """
    )
    public ResponseEntity<ContextPackDetailDTO> create(
            @Valid @RequestBody ContextPackCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contextPackService.create(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar Context Pack")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contextPackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Endpoints disponibles

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/context-packs | Listar todos (con filtro ?category=) |
| GET | /api/context-packs/{id} | Ver pack completo con recursos |
| GET | /api/context-packs/popular | Top 5 más usados |
| POST | /api/context-packs | Crear nuevo pack |
| DELETE | /api/context-packs/{id} | Eliminar pack |

## Ejemplo de respuesta GET /api/context-packs

```json
[
  {
    "id": 1,
    "name": "Security Audit Pack",
    "description": "Todo lo necesario para auditorías de seguridad en código",
    "emoji": "🔒",
    "category": "security",
    "usageCount": 0,
    "tags": ["security", "owasp", "code-review"],
    "promptCount": 1,
    "skillCount": 1,
    "mcpServerCount": 2
  },
  {
    "id": 2,
    "name": "AI Development Pack",
    "description": "Recursos para desarrollar y documentar proyectos de IA",
    "emoji": "🤖",
    "category": "ai-development",
    "usageCount": 0,
    "tags": ["ai", "development", "documentation"],
    "promptCount": 3,
    "skillCount": 3,
    "mcpServerCount": 2
  }
]
```

## Ejemplo de respuesta GET /api/context-packs/1

```json
{
  "id": 1,
  "name": "Security Audit Pack",
  "description": "Todo lo necesario para auditorías de seguridad en código",
  "emoji": "🔒",
  "category": "security",
  "setupInstructions": "1. Configura GITHUB_TOKEN en tu MCP config\n2. Activa el MCP Filesystem apuntando a tu proyecto\n3. Usa la skill 'Bug Hunter' para análisis inicial",
  "tags": ["security", "owasp", "code-review"],
  "usageCount": 1,
  "prompts": [...],
  "skills": [...],
  "mcpServers": [...],
  "generatedMcpConfig": "{\n  \"mcpServers\": {\n    \"github\": {...},\n    \"filesystem\": {...}\n  }\n}"
}
```

## Verificación en Swagger
1. `GET /api/context-packs` → debe devolver 4 packs del seed
2. `GET /api/context-packs/1` → debe incluir prompts, skills, mcpServers y generatedMcpConfig
3. `POST /api/context-packs` con un pack personalizado → verificar que se crea
4. `GET /api/context-packs/popular` → verificar orden por usageCount
