### ISSUE 15: PromptController

**Comando**: `Implementa ISSUE 15`

**Archivo**: `backend/src/main/java/com/promptvault/controller/PromptController.java`

**Contenido**:

```java
package com.promptvault.controller;

import com.promptvault.dto.PromptCreateRequest;
import com.promptvault.dto.PromptDTO;
import com.promptvault.dto.PromptUpdateRequest;
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
}
```

**Verificar**: Compilar sin errores