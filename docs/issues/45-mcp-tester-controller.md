# Issue 45: [MCP-TESTER] MCPTesterController - Endpoints REST

## Objetivo
Exponer los endpoints de testing MCP con documentación Swagger detallada.

## Archivo a crear
`src/main/java/com/promptvault/controller/MCPTesterController.java`

## Implementación completa

```java
package com.promptvault.controller;

import com.promptvault.dto.MCPTestRequest;
import com.promptvault.dto.MCPTestRequest.TestLevel;
import com.promptvault.dto.MCPTestResult;
import com.promptvault.service.MCPTesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mcp-servers")
@RequiredArgsConstructor
@Tag(name = "MCP Tester", description = "Validación y test de conectividad de servidores MCP")
public class MCPTesterController {

    private final MCPTesterService testerService;

    @PostMapping("/{id}/test")
    @Operation(
        summary = "Testar servidor MCP por ID",
        description = """
            Valida la configuración de un servidor MCP almacenado.
            
            **Niveles de test:**
            - `STATIC`: Valida la estructura JSON (sin red). Rápido, siempre disponible.
            - `CONNECTIVITY`: STATIC + intenta conexión HTTP si el servidor tiene URL.
              Para servidores stdio (npx/uvx/node), el test de conectividad no aplica.
            
            **Casos de uso:**
            - Verificar que la config generada es correcta antes de copiarla a Claude Desktop
            - Detectar variables de entorno no configuradas (placeholders)
            - Confirmar que un servidor HTTP remoto está activo
            """
    )
    public ResponseEntity<MCPTestResult> testServer(
            @PathVariable Long id,
            @Parameter(description = "STATIC (solo JSON) o CONNECTIVITY (JSON + ping HTTP)")
            @RequestParam(defaultValue = "STATIC") TestLevel level) {
        return ResponseEntity.ok(testerService.testById(id, level));
    }

    @PostMapping("/test-config")
    @Operation(
        summary = "Validar configuración JSON directa",
        description = """
            Valida un JSON de configuración MCP sin necesidad de tenerlo guardado en BD.
            Útil para pegar una configuración copiada de internet y verificarla.
            
            **Ejemplo de body:**
            ```json
            {
              "configJson": "{\\\"mcpServers\\\": {\\\"github\\\": {\\\"command\\\": \\\"npx\\\", \\\"args\\\": [\\\"-y\\\", \\\"@modelcontextprotocol/server-github\\\"], \\\"env\\\": {\\\"GITHUB_TOKEN\\\": \\\"your_token\\\"}}}}",
              "level": "STATIC"
            }
            ```
            """
    )
    public ResponseEntity<MCPTestResult> testConfigJson(@RequestBody MCPTestRequest request) {
        return ResponseEntity.ok(
            testerService.testConfigJson(request.getConfigJson(), request.getLevel())
        );
    }

    @PostMapping("/test-all")
    @Operation(
        summary = "Testar todos los servidores MCP",
        description = """
            Ejecuta el test de validación en todos los servidores almacenados.
            Devuelve un array con el resultado de cada uno.
            
            **Nota**: Con nivel CONNECTIVITY puede tardar hasta 5s por servidor.
            Con 10 servidores y nivel STATIC el resultado es instantáneo.
            """
    )
    public ResponseEntity<List<MCPTestResult>> testAll(
            @RequestParam(defaultValue = "STATIC") TestLevel level) {
        return ResponseEntity.ok(testerService.testAll(level));
    }
}
```

## Endpoints disponibles

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | /api/mcp-servers/{id}/test | Testar servidor por ID |
| POST | /api/mcp-servers/test-config | Validar JSON directo |
| POST | /api/mcp-servers/test-all | Testar todos |

## Ejemplo de llamada con curl

```bash
# Test estático del servidor GitHub (ID 1)
curl -X POST "http://localhost:8080/api/mcp-servers/1/test?level=STATIC"

# Test de conectividad
curl -X POST "http://localhost:8080/api/mcp-servers/1/test?level=CONNECTIVITY"

# Validar config JSON directa
curl -X POST "http://localhost:8080/api/mcp-servers/test-config" \
  -H "Content-Type: application/json" \
  -d '{"configJson": "{\"mcpServers\": {\"test\": {\"command\": \"npx\"}}}", "level": "STATIC"}'

# Test de todos (nivel estático)
curl -X POST "http://localhost:8080/api/mcp-servers/test-all?level=STATIC"
```

## Ejemplo de respuesta esperada (servidor GitHub, nivel STATIC)

```json
{
  "serverId": 1,
  "serverName": "GitHub",
  "valid": true,
  "status": "WARNING",
  "issues": [
    {
      "severity": "WARNING",
      "field": "mcpServers.github.env.GITHUB_TOKEN",
      "message": "La variable de entorno 'GITHUB_TOKEN' parece un placeholder sin rellenar: 'your_github_token'",
      "suggestion": "Reemplaza 'your_github_token' con tu valor real de GITHUB_TOKEN"
    },
    {
      "severity": "INFO",
      "field": "mcpServers.github",
      "message": "Configuración del servidor 'github' es válida",
      "suggestion": "Recuerda añadir este bloque a tu claude_desktop_config.json o .vscode/mcp.json"
    }
  ],
  "connectivity": null,
  "testedAt": "2026-02-18T10:30:00"
}
```

## Verificación en Swagger
1. Ir a http://localhost:8080/swagger-ui.html
2. Sección "MCP Tester"
3. Ejecutar `POST /api/mcp-servers/1/test?level=STATIC`
4. Verificar que detecta el placeholder `your_github_token` como WARNING
5. Ejecutar `POST /api/mcp-servers/test-all` y verificar que hay 10 resultados
