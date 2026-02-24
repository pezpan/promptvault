# Issue 56: [WORKFLOWS] WorkflowController — Endpoints REST

## Archivo a crear
`src/main/java/com/promptvault/controller/WorkflowController.java`

```java
package com.promptvault.controller;

import com.promptvault.dto.*;
import com.promptvault.service.WorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
@Tag(name = "Workflows", description = "Cadenas de prompts con contexto acumulativo — ejecución automática multi-paso")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    @Operation(
        summary = "Listar todos los workflows",
        description = "Devuelve todos los workflows con un resumen de sus pasos."
    )
    public ResponseEntity<List<WorkflowDTO>> findAll(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(workflowService.findByCategory(category));
        }
        return ResponseEntity.ok(workflowService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Ver workflow completo",
        description = "Devuelve la definición completa del workflow con todos sus pasos."
    )
    public ResponseEntity<WorkflowDetailDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.findById(id));
    }

    @GetMapping("/popular")
    @Operation(summary = "Top 5 workflows más ejecutados")
    public ResponseEntity<List<WorkflowDTO>> findPopular() {
        return ResponseEntity.ok(workflowService.findPopular());
    }

    @PostMapping
    @Operation(
        summary = "Crear nuevo workflow",
        description = """
            Crea un workflow con una secuencia de pasos. Tipos de paso disponibles:
            
            **SKILL** — Usa una Skill del catálogo (requiere skillId):
            ```json
            {
              "name": "Revisar código",
              "stepType": "SKILL",
              "skillId": 1,
              "skillParameters": {
                "CODE": "__PREVIOUS_OUTPUT__",
                "LANGUAGE": "Java"
              }
            }
            ```
            El valor especial `__PREVIOUS_OUTPUT__` indica que ese parámetro
            se rellena automáticamente con el output del paso anterior.
            
            **FREE_PROMPT** — Prompt libre (usa `{{PREVIOUS_OUTPUT}}` para insertar el output anterior):
            ```json
            {
              "name": "Generar resumen ejecutivo",
              "stepType": "FREE_PROMPT",
              "promptTemplate": "Genera un resumen ejecutivo de máximo 3 párrafos basándote en:\\n{{PREVIOUS_OUTPUT}}"
            }
            ```
            
            **TRANSFORM** — Transformación predefinida sin configuración:
            ```json
            {
              "name": "Formatear como Markdown",
              "stepType": "TRANSFORM",
              "transformType": "FORMAT_MARKDOWN"
            }
            ```
            Valores válidos de transformType: SUMMARIZE, TRANSLATE_ES, TRANSLATE_EN, FORMAT_MARKDOWN, EXTRACT_KEYWORDS
            """
    )
    public ResponseEntity<WorkflowDetailDTO> create(
            @Valid @RequestBody WorkflowCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.create(request));
    }

    @PostMapping("/{id}/execute")
    @Operation(
        summary = "Ejecutar workflow",
        description = """
            Ejecuta el workflow con el input inicial proporcionado.
            
            El sistema ejecuta cada paso en secuencia con **contexto acumulativo**:
            cada paso recibe el output de todos los pasos anteriores, lo que permite
            que el modelo mantenga coherencia a lo largo de toda la cadena.
            
            La respuesta incluye:
            - `finalOutput`: el resultado del último paso
            - `stepResults`: el detalle de cada paso (input enviado, output recibido, tiempo)
            - `totalExecutionTimeMs`: tiempo total de ejecución
            - `totalTokensUsed`: estimación de tokens consumidos
            
            **⚠️ Nota de rendimiento**: Cada paso realiza una llamada a Groq API.
            Un workflow de 3 pasos hace 3 llamadas. Con el límite de 30 req/min de Groq,
            esto es completamente viable para demos y uso normal.
            """
    )
    public ResponseEntity<WorkflowExecutionResult> execute(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowExecuteRequest request) {
        return ResponseEntity.ok(workflowService.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar workflow")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workflowService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Endpoints disponibles

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | /api/workflows | Listar todos (con filtro ?category=) |
| GET | /api/workflows/{id} | Ver workflow completo con pasos |
| GET | /api/workflows/popular | Top 5 más ejecutados |
| POST | /api/workflows | Crear nuevo workflow |
| POST | /api/workflows/{id}/execute | **Ejecutar workflow** |
| DELETE | /api/workflows/{id} | Eliminar workflow |

## Verificación
```bash
mvn clean compile && mvn spring-boot:run
# El endpoint /api/workflows debe aparecer en Swagger
```
