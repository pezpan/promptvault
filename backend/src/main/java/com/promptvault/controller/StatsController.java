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
