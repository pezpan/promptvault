package com.promptvault.controller;

import com.promptvault.dto.GenerateConfigRequest; // Import for GenerateConfigRequest
import com.promptvault.dto.MCPServerCreateRequest;
import com.promptvault.dto.MCPServerDTO;
import com.promptvault.model.MCPCategory; // Import for MCPCategory
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

import java.util.Arrays; // Import for Arrays
import java.util.List;
import java.util.Map; // Import for Map

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
        @RequestParam(required = false) String tag,
        @RequestParam(required = false) Boolean verified
    ) {
        Page<MCPServerDTO> servers;

        if (search != null && !search.isBlank()) {
            servers = mcpServerService.searchMCPServers(search, pageable);
        } else if (category != null && !category.isBlank()) {
            servers = mcpServerService.getMCPServersByCategory(category, pageable);
        } else if (tag != null && !tag.isBlank()) {
            servers = mcpServerService.getMCPServersByTag(tag, pageable);
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

    // NEW ENDPOINT: Generate Configuration
    @PostMapping("/generate-config")
    @Operation(summary = "Genera archivo de configuración para múltiples servidores MCP")
    public ResponseEntity<String> generateConfig(@RequestBody GenerateConfigRequest request) {
        String config = mcpServerService.generateConfig(request.getServerIds(), request.getEnvVars());
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .header("Content-Disposition", "attachment; filename=\"mcp-config.json\"")
            .body(config);
    }

    // NEW ENDPOINT: Get Categories
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
}
