### ISSUE 16: CategoryController

**Comando**: `Implementa ISSUE 16`

**Archivo**: `backend/src/main/java/com/promptvault/controller/CategoryController.java`

**Contenido**:

```java
package com.promptvault.controller;

import com.promptvault.model.Category;
import com.promptvault.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "API para gestión de categorías")
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    @Operation(summary = "Listar todas las categorías disponibles")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
}
```

**Verificar**:
```bash
mvn clean compile
mvn spring-boot:run
# Abrir Swagger: http://localhost:8080/swagger-ui.html
# Probar endpoints:
# - POST /api/prompts (crear prompt)
# - GET /api/prompts (listar)
# - GET /api/categories (debe retornar 5 categorías)
```