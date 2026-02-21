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
              "setupInstructions": "1. Configurar GITHUB_TOKEN
2. Instalar puppeteer",
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
