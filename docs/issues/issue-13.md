### ISSUE 13: PromptDTO

**Comando**: `Implementa ISSUE 13`

**Archivos a crear**:
1. `backend/src/main/java/com/promptvault/dto/PromptDTO.java`
2. `backend/src/main/java/com/promptvault/dto/PromptCreateRequest.java`
3. `backend/src/main/java/com/promptvault/dto/PromptUpdateRequest.java`

**PromptDTO.java**:
```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptDTO {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String category;
    private String[] tags;
    private String project;
    private Boolean isFavorite;
    private Integer usageCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**PromptCreateRequest.java**:
```java
package com.promptvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptCreateRequest {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede exceder 255 caracteres")
    private String title;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
    
    @NotBlank(message = "El contenido es obligatorio")
    private String content;
    
    @NotBlank(message = "La categoría es obligatoria")
    private String category;
    
    private String[] tags;
    
    private String project;
}
```

**PromptUpdateRequest.java**:
```java
package com.promptvault.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptUpdateRequest {
    
    @Size(max = 255)
    private String title;
    
    @Size(max = 500)
    private String description;
    
    private String content;
    
    private String category;
    
    private String[] tags;
    
    private String project;
    
    private Boolean isFavorite;
}
```

**Verificar**: Compilar sin errores
