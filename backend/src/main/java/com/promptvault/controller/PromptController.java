package com.promptvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.promptvault.dto.AIImprovementResponse;
import com.promptvault.dto.PromptCreateRequest;
import com.promptvault.dto.PromptDTO;
import com.promptvault.dto.PromptUpdateRequest;
import com.promptvault.service.AIEnhancementService;
import com.promptvault.service.PromptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@Tag(name = "Prompts", description = "API para gestión de prompts")
public class PromptController {
    
    private final PromptService promptService;
    private final AIEnhancementService aiEnhancementService;
    
    @PostMapping
    @Operation(summary = "Crear un nuevo prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Prompt creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<PromptDTO> createPrompt(@Valid @RequestBody PromptCreateRequest request) {
        PromptDTO created = promptService.createPrompt(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping
    @Operation(summary = "Listar todos los prompts con paginación")
    public ResponseEntity<Page<PromptDTO>> getAllPrompts(
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category
    ) {
        Page<PromptDTO> prompts;
        
        if (search != null && !search.isBlank()) {
            prompts = promptService.searchPrompts(search, pageable);
        } else if (category != null && !category.isBlank()) {
            prompts = promptService.getPromptsByCategory(category, pageable);
        } else {
            prompts = promptService.getAllPrompts(pageable);
        }
        
        return ResponseEntity.ok(prompts);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un prompt por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prompt encontrado"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado")
    })
    public ResponseEntity<PromptDTO> getPromptById(@PathVariable Long id) {
        PromptDTO prompt = promptService.getPromptById(id);
        return ResponseEntity.ok(prompt);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un prompt existente")
    public ResponseEntity<PromptDTO> updatePrompt(
        @PathVariable Long id,
        @Valid @RequestBody PromptUpdateRequest request
    ) {
        PromptDTO updated = promptService.updatePrompt(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Prompt eliminado"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado")
    })
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id) {
        promptService.deletePrompt(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mejora un prompt usando IA (Groq API).
     */
    @PostMapping("/{id}/improve")
    @Operation(summary = "Mejorar un prompt usando IA", 
               description = "Utiliza Groq API para analizar y mejorar la estructura y claridad del prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prompt mejorado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error en la API de IA")
    })
    public ResponseEntity<AIImprovementResponse> improvePrompt(@PathVariable Long id) {
        AIImprovementResponse improved = aiEnhancementService.improvePrompt(id);
        return ResponseEntity.ok(improved);
    }

    /**
     * Exporta un prompt a formato de archivo.
     */
    @GetMapping("/{id}/export")
    @Operation(summary = "Exportar un prompt a archivo", 
               description = "Descarga el prompt en formato .txt, .md o .json")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Archivo descargado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado")
    })
    public ResponseEntity<String> exportPrompt(
        @PathVariable Long id,
        @RequestParam(defaultValue = "txt") String format
    ) {
        PromptDTO prompt = promptService.getPromptById(id);
        
        String content;
        String contentType;
        String filename = sanitizeFilename(prompt.getTitle()) + "." + format;
        
        switch (format.toLowerCase()) {
            case "md":
            case "markdown":
                content = exportAsMarkdown(prompt);
                contentType = "text/markdown";
                filename = sanitizeFilename(prompt.getTitle()) + ".md";
                break;
                
            case "json":
                content = exportAsJson(prompt);
                contentType = "application/json";
                filename = sanitizeFilename(prompt.getTitle()) + ".json";
                break;
                
            case "txt":
            default:
                content = exportAsText(prompt);
                contentType = "text/plain";
                filename = sanitizeFilename(prompt.getTitle()) + ".txt";
                break;
        }
        
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .header("Content-Type", contentType + "; charset=UTF-8")
            .body(content);
    }
    
    /**
     * Exporta el prompt como texto plano.
     */
    private String exportAsText(PromptDTO prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("TÍTULO: ").append(prompt.getTitle()).append("\n\n");
        
        if (prompt.getDescription() != null && !prompt.getDescription().isBlank()) {
            sb.append("DESCRIPCIÓN: ").append(prompt.getDescription()).append("\n\n");
        }
        
        sb.append("CATEGORÍA: ").append(prompt.getCategory()).append("\n");
        
        if (prompt.getTags() != null && prompt.getTags().size() > 0) {
            sb.append("TAGS: ").append(String.join(", ", prompt.getTags())).append("\n");
        }
        
        sb.append("\n");
        sb.append("CONTENIDO:\n");
        sb.append("═".repeat(50)).append("\n\n");
        sb.append(prompt.getContent());
        
        return sb.toString();
    }
    
    /**
     * Exporta el prompt como Markdown.
     */
    private String exportAsMarkdown(PromptDTO prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(prompt.getTitle()).append("\n\n");
        
        if (prompt.getDescription() != null && !prompt.getDescription().isBlank()) {
            sb.append("> ").append(prompt.getDescription()).append("\n\n");
        }
        
        sb.append("**Categoría**: ").append(prompt.getCategory()).append("  \n");
        
        if (prompt.getTags() != null && prompt.getTags().size() > 0) {
            sb.append("**Tags**: ");
            for (String tag : prompt.getTags()) {
                sb.append("`").append(tag).append("` ");
            }
            sb.append("\n\n");
        }
        
        sb.append("---\n\n");
        sb.append("## Contenido\n\n");
        sb.append(prompt.getContent());
        
        return sb.toString();
    }
    
    /**
     * Exporta el prompt como JSON.
     */
    private String exportAsJson(PromptDTO prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(prompt);
        } catch (Exception e) {
            return "{\"error\": \"Error serializando a JSON\"}";
        }
    }
    
    /**
     * Sanitiza el nombre de archivo eliminando caracteres no válidos.
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "prompt";
        return filename
            .replaceAll("[^a-zA-Z0-9-_\\s]", "")
            .replaceAll("\\s+", "-")
            .toLowerCase()
            .substring(0, Math.min(filename.length(), 50));
    }
}
