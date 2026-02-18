# ISSUE 26: Crear Entidad MCPServer y Repository

Crea la entidad JPA para almacenar información de servidores MCP (Model Context Protocol).

## Contexto

MCP (Model Context Protocol) es el nuevo estándar para conectar herramientas externas a LLMs (Claude, GPT, etc.).

Ejemplos de MCP Servers:
- GitHub: Acceso a repositorios, issues, PRs
- Slack: Enviar mensajes, leer canales
- PostgreSQL: Consultar bases de datos
- Filesystem: Leer/escribir archivos

Esta funcionalidad permitirá a los usuarios descubrir y configurar MCP servers fácilmente.

---

## Archivos a Crear

### 1. MCPServer.java (Entity)

**Ruta**: `backend/src/main/java/com/promptvault/model/MCPServer.java`

**Contenido COMPLETO**:

```java
package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un servidor MCP (Model Context Protocol).
 * 
 * Los servidores MCP permiten conectar herramientas externas (GitHub, Slack, etc.)
 * a LLMs como Claude o GPT.
 */
@Entity
@Table(name = "mcp_servers", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_verified", columnList = "verified"),
    @Index(name = "idx_usage_count", columnList = "usage_count")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MCPServer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(name = "tags")
    private String[] tags;
    
    @Column(nullable = false, length = 100)
    private String command;
    
    @Column(name = "args", columnDefinition = "TEXT")
    private String args;  // JSON array como string: ["arg1", "arg2"]
    
    @Column(name = "env_vars", columnDefinition = "TEXT")
    private String envVars;  // JSON object como string: {"KEY": "value"}
    
    @Column(name = "capabilities", columnDefinition = "TEXT")
    private String capabilities;  // JSON array: ["read", "write"]
    
    @Column(name = "documentation", length = 500)
    private String documentation;
    
    @Column(name = "official_url", length = 500)
    private String officialUrl;
    
    @Column(name = "installation_instructions", columnDefinition = "TEXT")
    private String installationInstructions;
    
    @Column(name = "config_example", columnDefinition = "TEXT")
    private String configExample;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "verified")
    @Builder.Default
    private Boolean verified = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

---

### 2. MCPServerRepository.java

**Ruta**: `backend/src/main/java/com/promptvault/repository/MCPServerRepository.java`

**Contenido COMPLETO**:

```java
package com.promptvault.repository;

import com.promptvault.model.MCPServer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad MCPServer.
 */
@Repository
public interface MCPServerRepository extends JpaRepository<MCPServer, Long> {
    
    /**
     * Busca servidores MCP por categoría.
     */
    Page<MCPServer> findByCategory(String category, Pageable pageable);
    
    /**
     * Busca servidores MCP que contengan un tag específico.
     */
    @Query("SELECT m FROM MCPServer m WHERE :tag = ANY(m.tags)")
    Page<MCPServer> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    /**
     * Busca servidores MCP verificados.
     */
    Page<MCPServer> findByVerifiedTrue(Pageable pageable);
    
    /**
     * Búsqueda por texto en nombre y descripción.
     */
    @Query("SELECT m FROM MCPServer m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<MCPServer> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Obtiene los servidores más usados.
     */
    List<MCPServer> findTop10ByOrderByUsageCountDesc();
    
    /**
     * Busca servidores por categoría y verificados.
     */
    Page<MCPServer> findByCategoryAndVerifiedTrue(String category, Pageable pageable);
}
```

---

## Verificación

```bash
cd backend
mvn clean compile
```

Debe compilar sin errores.

## Notas

- La tabla `mcp_servers` se creará automáticamente al arrancar la aplicación
- Los campos JSON (args, envVars, capabilities) se almacenan como TEXT
- Se parsearán a JSON en el servicio layer
- Índices en `category`, `verified` y `usage_count` para búsquedas rápidas
- Campo `verified` indica si el servidor es oficial/confiable
- Campo `usageCount` para tracking de popularidad
- Campo `rating` para futuras valoraciones de usuarios
