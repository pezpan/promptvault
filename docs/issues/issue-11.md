### ISSUE 11: CategoryRepository

**Comando**: `Implementa ISSUE 11`

**Archivo**: `backend/src/main/java/com/promptvault/repository/CategoryRepository.java`

**Contenido**:
```java
package com.promptvault.repository;

import com.promptvault.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Category.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
}
```

**Verificar**: Compilar sin errores
