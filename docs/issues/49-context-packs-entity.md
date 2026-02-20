# Issue 49: [CONTEXT-PACKS] Entidad ContextPack y modelo de datos

## Concepto
Un Context Pack es un bundle preconfigurado que agrupa los recursos de PromptVault
necesarios para trabajar en un dominio concreto:
- Un conjunto de **Prompts** relevantes
- Las **Skills** más útiles para ese dominio
- Los **MCP Servers** recomendados
- Instrucciones de configuración listas para copiar

**Ejemplos de packs:**
- 🔒 "Security Audit Pack" → prompts de análisis + skill de vulnerabilidades + MCP GitHub + MCP Filesystem
- 📝 "Content Writing Pack" → prompts de redacción + skill de copywriting + MCP Brave Search
- 🗄️ "Database Dev Pack" → prompts SQL + skill de schema review + MCP PostgreSQL + MCP SQLite
- 🤖 "AI Development Pack" → prompts de prompt engineering + todas las skills + MCPs de filesystem y fetch

## Archivo a crear
`src/main/java/com/promptvault/model/ContextPack.java`

```java
package com.promptvault.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "context_packs")
@Data
public class ContextPack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    // Ejemplo: "Security Audit Pack"

    @Column(length = 500)
    private String description;
    // Ejemplo: "Todo lo necesario para auditorías de seguridad en código"

    private String emoji;
    // Emoji representativo: "🔒", "📝", "🗄️"

    private String category;
    // "security", "writing", "database", "ai-development", "devops"

    // IDs de prompts incluidos (relación ligera para no complicar el schema)
    @ElementCollection
    @CollectionTable(name = "context_pack_prompts", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "prompt_id")
    private List<Long> promptIds;

    // IDs de skills incluidas
    @ElementCollection
    @CollectionTable(name = "context_pack_skills", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "skill_id")
    private List<Long> skillIds;

    // IDs de MCP servers incluidos
    @ElementCollection
    @CollectionTable(name = "context_pack_mcps", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "mcp_id")
    private List<Long> mcpServerIds;

    // Instrucciones de setup en Markdown
    @Column(columnDefinition = "TEXT")
    private String setupInstructions;

    // Tags para búsqueda
    @ElementCollection
    @CollectionTable(name = "context_pack_tags", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "tag")
    private List<String> tags;

    private int usageCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## Archivo a crear
`src/main/java/com/promptvault/repository/ContextPackRepository.java`

```java
package com.promptvault.repository;

import com.promptvault.model.ContextPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContextPackRepository extends JpaRepository<ContextPack, Long> {
    List<ContextPack> findByCategory(String category);
    List<ContextPack> findByNameContainingIgnoreCase(String name);
    List<ContextPack> findTop5ByOrderByUsageCountDesc();
}
```

## Verificación
```bash
mvn clean compile
# Al arrancar, H2 creará las tablas context_packs, context_pack_prompts,
# context_pack_skills, context_pack_mcps, context_pack_tags automáticamente
```
