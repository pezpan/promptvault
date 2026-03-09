package com.promptvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "context_packs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private List<Long> promptIds = new ArrayList<>();

    // IDs de skills incluidas
    @ElementCollection
    @CollectionTable(name = "context_pack_skills", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "skill_id")
    @Builder.Default
    private List<Long> skillIds = new ArrayList<>();

    // IDs de MCP servers incluidos
    @ElementCollection
    @CollectionTable(name = "context_pack_mcps", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "mcp_id")
    @Builder.Default
    private List<Long> mcpServerIds = new ArrayList<>();

    // Instrucciones de setup en Markdown
    @Column(columnDefinition = "TEXT")
    private String setupInstructions;

    // Tags para búsqueda
    @ElementCollection
    @CollectionTable(name = "context_pack_tags", joinColumns = @JoinColumn(name = "pack_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
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
