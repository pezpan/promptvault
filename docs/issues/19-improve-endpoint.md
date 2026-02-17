# ISSUE 19: Endpoint POST /api/prompts/{id}/improve

Crea el endpoint REST para mejorar prompts con IA.

## Archivo a Modificar

**Ruta**: `backend/src/main/java/com/promptvault/controller/PromptController.java`

**Añadir al final de la clase** (antes del último `}`):

```java
    /**
     * Mejora un prompt usando IA (Gemini API).
     */
    @PostMapping("/{id}/improve")
    @Operation(summary = "Mejorar un prompt usando IA", 
               description = "Utiliza Gemini API para analizar y mejorar la estructura y claridad del prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prompt mejorado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error en la API de IA")
    })
    public ResponseEntity<AIImprovementResponse> improvePrompt(@PathVariable Long id) {
        AIImprovementResponse improved = aiEnhancementService.improvePrompt(id);
        return ResponseEntity.ok(improved);
    }
```

**Añadir también el import y la dependencia**:

En la parte superior de la clase, añadir:

```java
import com.promptvault.dto.AIImprovementResponse;
```

En el constructor, añadir el servicio:

```java
@RestController
@RequestMapping("/api/prompts")
@RequiredArgsConstructor
@Tag(name = "Prompts", description = "API para gestión de prompts")
public class PromptController {
    
    private final PromptService promptService;
    private final AIEnhancementService aiEnhancementService;  // AÑADIR ESTA LÍNEA
    
    // ... resto del código
```

## Código Completo del Método

Si prefieres ver el contexto completo, el método se añade así:

```java
package com.promptvault.controller;

import com.promptvault.dto.AIImprovementResponse;  // NUEVO IMPORT
import com.promptvault.dto.PromptCreateRequest;
import com.promptvault.dto.PromptDTO;
import com.promptvault.dto.PromptUpdateRequest;
import com.promptvault.service.AIEnhancementService;  // NUEVO IMPORT
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
    private final AIEnhancementService aiEnhancementService;  // NUEVO
    
    // ... todos los métodos existentes ...
    
    @PostMapping("/{id}/improve")
    @Operation(summary = "Mejorar un prompt usando IA", 
               description = "Utiliza Gemini API para analizar y mejorar la estructura y claridad del prompt")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Prompt mejorado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Prompt no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error en la API de IA")
    })
    public ResponseEntity<AIImprovementResponse> improvePrompt(@PathVariable Long id) {
        AIImprovementResponse improved = aiEnhancementService.improvePrompt(id);
        return ResponseEntity.ok(improved);
    }
}
```

## Verificación

```bash
mvn clean compile
mvn spring-boot:run
```

Abrir Swagger: http://localhost:8080/swagger-ui.html

Buscar el nuevo endpoint: **POST /api/prompts/{id}/improve**

### Probar el Endpoint

1. Primero obtén el ID de un prompt existente:
   ```
   GET /api/prompts
   ```

2. Usa ese ID para mejorar el prompt:
   ```
   POST /api/prompts/1/improve
   ```

3. Debería retornar:
   ```json
   {
     "originalContent": "Contenido original...",
     "improvedContent": "Versión mejorada con estructura...",
     "improvements": [
       "Añadida sección de CONTEXTO",
       "Estructuradas las instrucciones en pasos",
       "Definido formato de salida"
     ],
     "tokenUsage": null
   }
   ```

## Requisitos Previos

- **GEMINI_API_KEY** debe estar configurada (Issue 17)
- Tener al menos un prompt en la base de datos (los 3 de ejemplo del Issue 12)

## Notas

- El endpoint llama a Gemini API en tiempo real (puede tardar 2-5 segundos)
- Si Gemini API falla, retorna error 500 con mensaje descriptivo
- La mejora NO modifica el prompt original, solo retorna la versión mejorada
- El usuario puede decidir si aplicar o no la mejora
