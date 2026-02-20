# Issue 40: [STATS] Verificación del Dashboard con datos reales

## Objetivo
Verificar que todos los endpoints de estadísticas funcionan correctamente con los datos
del seed (3 prompts, 10 MCP servers, 5 skills) y tras hacer mejoras con IA.

## Checklist de verificación

### Paso 1: Compilar y arrancar
```bash
cd backend
mvn clean compile   # debe compilar sin errores
mvn spring-boot:run # debe arrancar en puerto 8080
```

### Paso 2: Verificar datos base (GET /api/stats/global)
Esperado con datos de seed sin usar IA:
```json
{
  "totalPrompts": 3,
  "totalImprovements": 0,
  "totalMcpServers": 10,
  "totalSkills": 5,
  "improvementRatio": 0.0
}
```
- [ ] `totalPrompts` = 3
- [ ] `totalMcpServers` = 10
- [ ] `totalSkills` = 5
- [ ] `improvementRatio` = 0.0

### Paso 3: Verificar distribución por categoría (GET /api/stats/prompts)
```json
{
  "promptsByCategory": { ... },
  "improvedPrompts": 0,
  "notImprovedPrompts": 3
}
```
- [ ] `promptsByCategory` no está vacío
- [ ] `improvedPrompts` + `notImprovedPrompts` = `totalPrompts`

### Paso 4: Verificar estadísticas de IA (GET /api/stats/ai)
```json
{
  "mostUsedSkill": "Code Reviewer Expert",
  "mostPopularMcpServer": "GitHub",
  "totalAICallsMade": 0,
  "recentImprovements": []
}
```
- [ ] `mostUsedSkill` no es null (debe ser el nombre de la primera skill)
- [ ] `mostPopularMcpServer` no es null (debe ser "GitHub")
- [ ] `recentImprovements` es lista vacía (aún no se han hecho mejoras)

### Paso 5: Probar con mejora real de IA
```bash
# Hacer una mejora con Groq (necesita API key configurada)
curl -X POST http://localhost:8080/api/prompts/1/improve
```

Luego volver a llamar `GET /api/stats/global`:
- [ ] `totalImprovements` = 1
- [ ] `improvementRatio` > 0.0

Y `GET /api/stats/ai`:
- [ ] `recentImprovements` tiene 1 elemento con `promptId`, `promptTitle` e `improvedAt`

### Paso 6: Verificar GET /api/stats (dashboard completo)
- [ ] Response contiene los tres bloques: `global`, `prompts`, `ai`
- [ ] `generatedAt` tiene un timestamp reciente (no null)

## Posibles errores y soluciones

| Error | Causa probable | Solución |
|-------|---------------|----------|
| `NullPointerException` en `mostUsedSkill` | Skills tienen `usageCount` null | Añadir `DEFAULT 0` en `skills-data.sql` |
| `countByCategory()` devuelve lista vacía | `JOIN` falla porque categoría es null | Verificar que todos los prompts del seed tienen `category_id` |
| `findTopByOrderByUsageCountDesc()` vacío | No hay registros en la tabla | Verificar que el seed se ejecutó con `spring.jpa.defer-datasource-initialization=true` |
| `countImprovedPrompts()` siempre 0 | Campo `lastImprovedAt` no existe | Añadir campo al modelo `Prompt.java` (ver Issue 38) |
