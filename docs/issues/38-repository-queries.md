# Issue 38: [STATS] Agregar queries de estadísticas a los Repositories

## Objetivo
Añadir métodos de agregación a los repositories existentes sin modificar su estructura base.

## Archivos a modificar

### `PromptRepository.java`
Añadir al final de la interfaz:
```java
// Contar prompts por categoría
@Query("SELECT c.name, COUNT(p) FROM Prompt p JOIN p.category c GROUP BY c.name")
List<Object[]> countByCategory();

// Contar prompts que han sido mejorados al menos una vez
@Query("SELECT COUNT(p) FROM Prompt p WHERE p.lastImprovedAt IS NOT NULL")
long countImprovedPrompts();
```

> **Nota**: Requiere `import org.springframework.data.jpa.repository.Query;` y
> `import java.util.List;`

---

### `SkillRepository.java`
Añadir al final de la interfaz:
```java
// La skill más usada (por usageCount)
Optional<Skill> findTopByOrderByUsageCountDesc();
```

> **Nota**: Spring Data JPA resuelve este método automáticamente por convención de nombres.
> Requiere `import java.util.Optional;`

---

### `MCPServerRepository.java`
Añadir al final de la interfaz:
```java
// El MCP server más popular (por usageCount)
Optional<MCPServer> findTopByOrderByUsageCountDesc();
```

---

### `PromptImprovementRepository.java`
Añadir al final de la interfaz:
```java
// Las 5 mejoras más recientes
List<PromptImprovement> findTop5ByOrderByCreatedAtDesc();
```

---

## Verificación de Campos Requeridos

Asegurarse de que el modelo `Prompt.java` tiene el campo `lastImprovedAt`:
```java
private LocalDateTime lastImprovedAt;
```
Si no existe, añadirlo y actualizar el endpoint `/improve` para que lo setee al hacer una mejora:
```java
// En PromptService.java, al guardar la mejora:
prompt.setLastImprovedAt(LocalDateTime.now());
promptRepository.save(prompt);
```

## Test rápido con H2 Console
Tras arrancar la aplicación, abrir http://localhost:8080/h2-console y ejecutar:
```sql
-- Verificar la query de categorías
SELECT c.name, COUNT(p.id)
FROM prompts p
JOIN categories c ON p.category_id = c.id
GROUP BY c.name;
```
