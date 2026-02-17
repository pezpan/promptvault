# ISSUE 20: Endpoint GET /api/prompts/{id}/export

Crea el endpoint para exportar prompts a archivos.

## Archivo a Modificar

**Ruta**: `backend/src/main/java/com/promptvault/controller/PromptController.java`

**Añadir al final de la clase** (antes del último `}`):

```java
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
        
        if (prompt.getTags() != null && prompt.getTags().length > 0) {
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
        
        if (prompt.getTags() != null && prompt.getTags().length > 0) {
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
```

**Añadir import necesario** (al inicio del archivo):

```java
import com.fasterxml.jackson.databind.ObjectMapper;
```

## Verificación

```bash
mvn clean compile
mvn spring-boot:run
```

Abrir Swagger: http://localhost:8080/swagger-ui.html

### Probar el Endpoint

1. Buscar: **GET /api/prompts/{id}/export**

2. Probar con diferentes formatos:

   ```
   GET /api/prompts/1/export?format=txt
   GET /api/prompts/1/export?format=md
   GET /api/prompts/1/export?format=json
   ```

3. El navegador debería descargar un archivo:
   - `java-bug-fixer.txt`
   - `java-bug-fixer.md`
   - `java-bug-fixer.json`

### Ejemplo de Salida

**Formato TXT**:
```
TÍTULO: Java Bug Fixer

DESCRIPCIÓN: Analiza y corrige bugs en código Java

CATEGORÍA: debugging
TAGS: java, spring-boot, bug-fix

CONTENIDO:
══════════════════════════════════════════════════

Analiza el siguiente código Java y encuentra el bug:
...
```

**Formato Markdown**:
```markdown
# Java Bug Fixer

> Analiza y corrige bugs en código Java

**Categoría**: debugging  
**Tags**: `java` `spring-boot` `bug-fix` 

---

## Contenido

Analiza el siguiente código Java y encuentra el bug:
...
```

**Formato JSON**:
```json
{
  "id": 1,
  "title": "Java Bug Fixer",
  "description": "Analiza y corrige bugs en código Java",
  "content": "Analiza el siguiente código...",
  "category": "debugging",
  "tags": ["java", "spring-boot", "bug-fix"],
  ...
}
```

## Notas

- Por defecto exporta en formato `.txt`
- Formatos soportados: `txt`, `md`, `markdown`, `json`
- El nombre del archivo se genera automáticamente desde el título del prompt
- Caracteres especiales en el título se eliminan automáticamente
- El archivo se descarga directamente (header `Content-Disposition: attachment`)
