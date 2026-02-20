package com.promptvault.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationIssue {
        private Severity severity;  // ERROR, WARNING, INFO
        private String field;       // Campo con problema (ej: "args", "env.API_KEY")
        private String message;     // Descripción del problema
        private String suggestion;  // Cómo arreglarlo

        public enum Severity { ERROR, WARNING, INFO }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConnectivityResult {
        private boolean attempted;
        private boolean reachable;
        private Integer httpStatusCode;
        private Long responseTimeMs;
        private String url;
        private String error;
    }
}
