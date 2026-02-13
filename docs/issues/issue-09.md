### ISSUE 09: Category Entity

**Comando**: `Implementa ISSUE 09`

**Archivo**: `backend/src/main/java/com/promptvault/model/Category.java`

**Contenido**:
```java
package com.promptvault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una categoría de prompts.
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 50)
    private String icon;
    
    @Column(length = 20)
    private String color;
}
```

**Verificar**: Compilar sin errores