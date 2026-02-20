package com.promptvault.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPTestRequest {
    // Si se proporciona serverId, se testa el servidor almacenado
    private Long serverId;

    // Si se proporciona configJson, se valida esa config directamente (sin BD)
    private String configJson;

    // Nivel de test: STATIC (solo JSON) o CONNECTIVITY (JSON + ping HTTP)
    @Builder.Default
    private TestLevel level = TestLevel.STATIC;

    public enum TestLevel {
        STATIC,       // Solo valida estructura JSON
        CONNECTIVITY  // JSON + intenta conexión HTTP si hay URL disponible
    }
}
