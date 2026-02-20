# Issue 41: [MCP-TESTER] Modelo de validación y DTOs

## Contexto
Añadir capacidad de validar configuraciones MCP en dos niveles:
- **Nivel 1 (estático)**: Validar que el JSON de config tiene la estructura correcta
- **Nivel 2 (conectividad)**: Intentar una conexión real HTTP al servidor si tiene URL

No requiere nueva entidad en BD. Solo DTOs de request/response y lógica de validación.

## Archivos a crear

### `src/main/java/com/promptvault/dto/MCPTestRequest.java`
```java
package com.promptvault.dto;

import lombok.Data;

@Data
public class MCPTestRequest {
    // Si se proporciona serverId, se testa el servidor almacenado
    private Long serverId;

    // Si se proporciona configJson, se valida esa config directamente (sin BD)
    private String configJson;

    // Nivel de test: STATIC (solo JSON) o CONNECTIVITY (JSON + ping HTTP)
    private TestLevel level = TestLevel.STATIC;

    public enum TestLevel {
        STATIC,       // Solo valida estructura JSON
        CONNECTIVITY  // JSON + intenta conexión HTTP si hay URL disponible
    }
}
```

### `src/main/java/com/promptvault/dto/MCPTestResult.java`
```java
package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MCPTestResult {
    private Long serverId;
    private String serverName;
    private boolean valid;
    private TestStatus status;
    private List<ValidationIssue> issues;
    private ConnectivityResult connectivity;
    private LocalDateTime testedAt;

    public enum TestStatus {
        OK,           // Todo correcto
        WARNING,      // Válido pero con advertencias
        ERROR,        // Errores en la configuración
        UNREACHABLE   // Config válida pero servidor no responde
    }

    @Data
    @Builder
    public static class ValidationIssue {
        private Severity severity;  // ERROR, WARNING, INFO
        private String field;       // Campo con problema (ej: "args", "env.API_KEY")
        private String message;     // Descripción del problema
        private String suggestion;  // Cómo arreglarlo

        public enum Severity { ERROR, WARNING, INFO }
    }

    @Data
    @Builder
    public static class ConnectivityResult {
        private boolean attempted;
        private boolean reachable;
        private Integer httpStatusCode;
        private Long responseTimeMs;
        private String url;
        private String error;
    }
}
```

## Verificación
```bash
mvn clean compile  # Sin errores
```
