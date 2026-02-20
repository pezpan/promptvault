# Issue 39: [STATS] Crear StatsController con endpoints del Dashboard

## Objetivo
Exponer los endpoints REST del dashboard de estadísticas con documentación Swagger.

## Archivo a crear
`src/main/java/com/promptvault/controller/StatsController.java`

## Implementación

```java
package com.promptvault.controller;

import com.promptvault.dto.*;
import com.promptvault.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Dashboard de métricas y estadísticas de uso de PromptVault")
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @Operation(
        summary = "Dashboard completo",
        description = "Retorna todas las métricas del sistema: globales, de prompts y de IA"
    )
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(statsService.getDashboard());
    }

    @GetMapping("/global")
    @Operation(
        summary = "Métricas globales",
        description = "Totales de prompts, mejoras, MCP servers, skills y ratio de mejora"
    )
    public ResponseEntity<GlobalStatsDTO> getGlobalStats() {
        return ResponseEntity.ok(statsService.getGlobalStats());
    }

    @GetMapping("/prompts")
    @Operation(
        summary = "Estadísticas de prompts",
        description = "Distribución por categoría y ratio de prompts mejorados"
    )
    public ResponseEntity<PromptStatsDTO> getPromptStats() {
        return ResponseEntity.ok(statsService.getPromptStats());
    }

    @GetMapping("/ai")
    @Operation(
        summary = "Estadísticas de uso de IA",
        description = "Skill más usada, MCP más popular, total de llamadas IA y mejoras recientes"
    )
    public ResponseEntity<AIStatsDTO> getAIStats() {
        return ResponseEntity.ok(statsService.getAIStats());
    }
}
```

## Endpoints disponibles tras implementar

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/stats | Dashboard completo (todos los datos) |
| GET | /api/stats/global | Solo métricas globales |
| GET | /api/stats/prompts | Solo estadísticas de prompts |
| GET | /api/stats/ai | Solo estadísticas de IA |

## Ejemplo de respuesta GET /api/stats

```json
{
  "global": {
    "totalPrompts": 3,
    "totalImprovements": 0,
    "totalMcpServers": 10,
    "totalSkills": 5,
    "improvementRatio": 0.0
  },
  "prompts": {
    "promptsByCategory": {
      "Development": 2,
      "Writing": 1
    },
    "improvedPrompts": 0,
    "notImprovedPrompts": 3
  },
  "ai": {
    "mostUsedSkill": "Code Reviewer Expert",
    "mostPopularMcpServer": "GitHub",
    "totalAICallsMade": 0,
    "recentImprovements": []
  },
  "generatedAt": "2026-02-18T10:30:00"
}
```

## Verificación en Swagger
1. Abrir http://localhost:8080/swagger-ui.html
2. Buscar sección "Statistics"
3. Ejecutar `GET /api/stats`
4. Verificar que el response tiene los 3 bloques: global, prompts, ai
