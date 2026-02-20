# Issue 43: [MCP-TESTER] MCPConnectivityService - Test de conectividad HTTP

## Objetivo
Servicio que intenta una conexión real al servidor MCP cuando éste expone una URL HTTP.
Para servidores locales (npx, uvx) solo reporta que el test de conectividad no aplica.

## Contexto sobre tipos de MCP Servers
Los MCP servers se dividen en dos tipos:
- **Stdio servers** (npx, uvx, node): Se ejecutan como proceso local. NO tienen URL.
  No se puede testear conectividad sin ejecutar el proceso. → Reportar como "N/A"
- **HTTP/SSE servers**: Exponen una URL HTTP. SÍ se puede hacer ping.
  Ejemplos: servidores self-hosted, APIs remotas MCP-compatibles.

## Archivo a crear
`src/main/java/com/promptvault/service/MCPConnectivityService.java`

## Implementación completa

```java
package com.promptvault.service;

import com.promptvault.dto.MCPTestResult.ConnectivityResult;
import com.promptvault.model.MCPServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
public class MCPConnectivityService {

    // Timeout corto: no queremos bloquear la respuesta
    private static final int TIMEOUT_SECONDS = 5;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();

    /**
     * Intenta conectar al servidor MCP.
     * - Si es stdio (npx/uvx/node/python): devuelve "not applicable"
     * - Si tiene URL HTTP: hace un GET con timeout de 5s
     */
    public ConnectivityResult testConnectivity(MCPServer server) {
        String command = server.getCommand();

        // Servidores stdio - no tienen URL accesible
        if (isStdioServer(command)) {
            return ConnectivityResult.builder()
                    .attempted(false)
                    .reachable(false)
                    .url(null)
                    .error("Servidor stdio (" + command + "): el test de conectividad HTTP no aplica. " +
                           "Este tipo de servidor se ejecuta como proceso local.")
                    .build();
        }

        // Servidores HTTP - intentar conexión
        String url = extractUrl(server);
        if (url == null) {
            return ConnectivityResult.builder()
                    .attempted(false)
                    .reachable(false)
                    .error("No se encontró URL en la configuración del servidor")
                    .build();
        }

        return pingUrl(url);
    }

    /**
     * Test de conectividad a una URL directa (sin MCPServer en BD)
     */
    public ConnectivityResult testUrl(String url) {
        if (url == null || url.isBlank()) {
            return ConnectivityResult.builder()
                    .attempted(false)
                    .reachable(false)
                    .error("URL vacía o nula")
                    .build();
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ConnectivityResult.builder()
                    .attempted(false)
                    .reachable(false)
                    .url(url)
                    .error("URL inválida: debe comenzar con http:// o https://")
                    .build();
        }

        return pingUrl(url);
    }

    private ConnectivityResult pingUrl(String url) {
        Instant start = Instant.now();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .header("User-Agent", "PromptVault-MCPTester/1.0")
                    .build();

            HttpResponse<Void> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.discarding()
            );

            long responseTimeMs = Duration.between(start, Instant.now()).toMillis();
            int statusCode = response.statusCode();

            // 2xx y 4xx cuentan como "reachable" (el servidor responde)
            // 5xx y timeouts = unreachable
            boolean reachable = statusCode < 500;

            return ConnectivityResult.builder()
                    .attempted(true)
                    .reachable(reachable)
                    .httpStatusCode(statusCode)
                    .responseTimeMs(responseTimeMs)
                    .url(url)
                    .error(reachable ? null : "Servidor respondió con error " + statusCode)
                    .build();

        } catch (java.net.http.HttpTimeoutException e) {
            return ConnectivityResult.builder()
                    .attempted(true)
                    .reachable(false)
                    .url(url)
                    .error("Timeout después de " + TIMEOUT_SECONDS + "s - el servidor no responde")
                    .build();
        } catch (Exception e) {
            log.warn("Error al conectar con {}: {}", url, e.getMessage());
            return ConnectivityResult.builder()
                    .attempted(true)
                    .reachable(false)
                    .url(url)
                    .error("Error de conexión: " + e.getMessage())
                    .build();
        }
    }

    private boolean isStdioServer(String command) {
        if (command == null) return true;
        return List.of("npx", "uvx", "node", "python", "python3", "deno")
                .contains(command.toLowerCase());
    }

    private String extractUrl(MCPServer server) {
        // Buscar URL en los args del servidor
        // Muchos MCP HTTP servers se configuran como:
        // { "command": "node", "args": ["server.js", "--url", "http://..."] }
        if (server.getArgs() != null) {
            for (String arg : server.getArgs()) {
                if (arg.startsWith("http://") || arg.startsWith("https://")) {
                    return arg;
                }
            }
        }
        // También buscar en documentationUrl si el modelo lo tiene
        if (server.getDocumentationUrl() != null && !server.getDocumentationUrl().isBlank()) {
            return server.getDocumentationUrl();
        }
        return null;
    }
}
```

## Nota sobre MCPServer.args
Si el modelo `MCPServer.java` no tiene campo `args` como `List<String>`, adaptar
`extractUrl()` para parsear el campo que almacena los argumentos del servidor.

## Dependencias
- Java 11+ HttpClient (nativo, sin dependencias adicionales)
- No requiere cambios en `pom.xml`

## Verificación
```bash
mvn clean compile
```
