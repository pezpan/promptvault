### ISSUE 08: Prompt Entity

**Comando**: `Implementa ISSUE 08`

**Archivo**: `backend/src/main/java/com/promptvault/model/Prompt.java`

**Contenido**:
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
 * Entidad que representa un prompt almacenado en el sistema.
 */
@Entity
@Table(name = "prompts", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_favorite", columnList = "is_favorite")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false, length = 50)
    private String category;
    
    @Column(name = "tags")
    private String[] tags;
    
    @Column(name = "project", length = 100)
    private String project;
    
    @Column(name = "is_favorite")
    @Builder.Default
    private Boolean isFavorite = false;
    
    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;
    
    @Column(length = 20)
    @Builder.Default
    private String status = "published";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Integer version;
}
```

**Verificar**: Compilar sin errores