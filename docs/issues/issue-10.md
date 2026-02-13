### ISSUE 10: PromptRepository

**Comando**: `Implementa ISSUE 10`

**Archivo**: `backend/src/main/java/com/promptvault/repository/PromptRepository.java`

**Contenido**:
```java
package com.promptvault.repository;

import com.promptvault.model.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Prompt.
 */
@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    Page<Prompt> findByCategory(String category, Pageable pageable);
    
    @Query("SELECT p FROM Prompt p WHERE :tag = ANY(p.tags)")
    Page<Prompt> findByTagsContaining(@Param("tag") String tag, Pageable pageable);
    
    Page<Prompt> findByIsFavoriteTrue(Pageable pageable);
    
    @Query("SELECT p FROM Prompt p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Prompt> searchByTitleOrContent(@Param("searchTerm") String searchTerm, Pageable pageable);
}
```

**Verificar**: Compilar sin errores
