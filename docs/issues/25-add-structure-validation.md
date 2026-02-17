# ISSUE 25: Añadir Metadatos de Validación a la Respuesta /improve

Añade información sobre qué secciones están presentes en el prompt mejorado para que el usuario vea visualmente la calidad de la mejora.

## Contexto

Después del Issue 24, los prompts mejorados usan la estructura de 5 secciones (ROL, TAREA, AUDIENCIA, FORMATO, CONTEXTO).

Esta mejora añade un campo `structureValidation` en la respuesta para que el usuario vea qué secciones están presentes.

---

## Archivos a Modificar

### 1. MODIFICAR: AIImprovementResponse.java (DTO)

**Archivo**: `backend/src/main/java/com/promptvault/dto/AIImprovementResponse.java`

**AÑADIR** un nuevo campo al final de la clase (antes del `TokenUsage`):

```java
/**
 * Validación de estructura (indica qué secciones están presentes).
 */
private StructureValidation structureValidation;

/**
 * Información sobre qué secciones del prompt están presentes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public static class StructureValidation {
    private boolean hasRol;
    private boolean hasTarea;
    private boolean hasAudiencia;
    private boolean hasFormato;
    private boolean hasContexto;
    private int completeness; // Porcentaje (0-100)
    private String quality; // "Excelente", "Buena", "Mejorable"
}
```

**El campo ya existente `TokenUsage` debe quedar DESPUÉS de `StructureValidation`**.

**Archivo completo resultante** (para referencia):

```java
package com.promptvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que contiene el resultado de mejorar un prompt con IA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIImprovementResponse {
    
    /**
     * Contenido original del prompt.
     */
    private String originalContent;
    
    /**
     * Contenido mejorado por la IA.
     */
    private String improvedContent;
    
    /**
     * Lista de mejoras realizadas (explicación).
     */
    private List<String> improvements;
    
    /**
     * Validación de estructura (indica qué secciones están presentes).
     */
    private StructureValidation structureValidation;
    
    /**
     * Tokens utilizados en la petición (opcional).
     */
    private TokenUsage tokenUsage;
    
    /**
     * Información sobre qué secciones del prompt están presentes.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StructureValidation {
        private boolean hasRol;
        private boolean hasTarea;
        private boolean hasAudiencia;
        private boolean hasFormato;
        private boolean hasContexto;
        private int completeness; // Porcentaje (0-100)
        private String quality; // "Excelente", "Buena", "Mejorable"
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage {
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
```

---

### 2. MODIFICAR: AIEnhancementService.java

**Archivo**: `backend/src/main/java/com/promptvault/service/AIEnhancementService.java`

**ACTUALIZAR** el método `parseImprovementResponse` para calcular y añadir la validación:

**REEMPLAZAR** el método completo por:

```java
/**
 * Parsea la respuesta de Groq y extrae el prompt mejorado y las mejoras.
 * Valida que el prompt mejorado contenga las 5 secciones requeridas.
 */
private AIImprovementResponse parseImprovementResponse(String originalContent, String groqResponse) {
    // Dividir respuesta en prompt mejorado y lista de mejoras
    String[] parts = groqResponse.split("---MEJORAS---");
    
    String improvedContent;
    List<String> improvements;
    
    if (parts.length >= 2) {
        improvedContent = parts[0].trim();
        
        // Parsear mejoras (líneas que empiezan con "- ")
        improvements = Arrays.stream(parts[1].split("\n"))
            .map(String::trim)
            .filter(line -> line.startsWith("-"))
            .map(line -> line.substring(1).trim())
            .toList();
    } else {
        // Si no encuentra el separador, usar toda la respuesta como mejora
        improvedContent = groqResponse.trim();
        improvements = List.of("Prompt mejorado con estructura profesional");
    }
    
    // Validar presencia de cada sección
    boolean hasRol = improvedContent.contains("[ROL]");
    boolean hasTarea = improvedContent.contains("[TAREA]");
    boolean hasAudiencia = improvedContent.contains("[AUDIENCIA]");
    boolean hasFormato = improvedContent.contains("[FORMATO]");
    boolean hasContexto = improvedContent.contains("[CONTEXTO");  // Acepta [CONTEXTO] o [CONTEXTO/DETALLES]
    
    // Calcular completitud (porcentaje de secciones presentes)
    int sectionsPresent = 0;
    if (hasRol) sectionsPresent++;
    if (hasTarea) sectionsPresent++;
    if (hasAudiencia) sectionsPresent++;
    if (hasFormato) sectionsPresent++;
    if (hasContexto) sectionsPresent++;
    
    int completeness = (sectionsPresent * 100) / 5;
    
    // Determinar calidad
    String quality;
    if (completeness == 100) {
        quality = "Excelente";
    } else if (completeness >= 80) {
        quality = "Buena";
    } else if (completeness >= 60) {
        quality = "Mejorable";
    } else {
        quality = "Incompleta";
    }
    
    // Crear objeto de validación
    AIImprovementResponse.StructureValidation validation = 
        AIImprovementResponse.StructureValidation.builder()
            .hasRol(hasRol)
            .hasTarea(hasTarea)
            .hasAudiencia(hasAudiencia)
            .hasFormato(hasFormato)
            .hasContexto(hasContexto)
            .completeness(completeness)
            .quality(quality)
            .build();
    
    // Log de validación
    if (completeness == 100) {
        log.info("✓ Prompt mejorado con estructura completa (100%): ROL, TAREA, AUDIENCIA, FORMATO, CONTEXTO");
    } else {
        log.warn("⚠ Estructura parcial ({}%): Faltan secciones", completeness);
        if (!hasRol) log.warn("  - Falta [ROL]");
        if (!hasTarea) log.warn("  - Falta [TAREA]");
        if (!hasAudiencia) log.warn("  - Falta [AUDIENCIA]");
        if (!hasFormato) log.warn("  - Falta [FORMATO]");
        if (!hasContexto) log.warn("  - Falta [CONTEXTO]");
    }
    
    // Si faltan secciones, añadir advertencia
    if (completeness < 100) {
        String note = String.format("""
            ⚠️ ADVERTENCIA: Este prompt mejorado tiene una completitud del %d%%
            Secciones faltantes pueden afectar la efectividad del prompt.
            Se recomienda revisar y completar manualmente las secciones faltantes.
            
            """, completeness);
        improvedContent = note + improvedContent;
        
        // Añadir info en mejoras
        List<String> updatedImprovements = new ArrayList<>(improvements);
        updatedImprovements.add(0, 
            String.format("⚠️ Estructura %s (%d%% completa) - revisar secciones faltantes", 
                quality, completeness));
        improvements = updatedImprovements;
    }
    
    return AIImprovementResponse.builder()
        .originalContent(originalContent)
        .improvedContent(improvedContent)
        .improvements(improvements)
        .structureValidation(validation)
        .tokenUsage(null)
        .build();
}
```

---

## Verificación

### Compilar
```bash
cd backend
mvn clean compile
```

### Ejecutar
```bash
mvn spring-boot:run
```

### Probar en Swagger

1. Abrir: http://localhost:8080/swagger-ui.html
2. POST /api/prompts/1/improve
3. Ejecutar

**Respuesta esperada** (con nuevo campo `structureValidation`):

```json
{
  "originalContent": "Analiza el siguiente código Java...",
  "improvedContent": "[ROL]\nActúa como desarrollador senior...\n\n[TAREA]\n1. Analiza...\n\n[AUDIENCIA]\nDesarrolladores...\n\n[FORMATO]\n- Código comentado...\n\n[CONTEXTO/DETALLES]\n- Java 17...",
  "improvements": [
    "Añadida sección [ROL] definiendo expertise",
    "Estructurada [TAREA] en pasos",
    "Especificada [AUDIENCIA]",
    "Definido [FORMATO] apropiado",
    "Incluido [CONTEXTO] técnico"
  ],
  "structureValidation": {
    "hasRol": true,
    "hasTarea": true,
    "hasAudiencia": true,
    "hasFormato": true,
    "hasContexto": true,
    "completeness": 100,
    "quality": "Excelente"
  },
  "tokenUsage": null
}
```

---

## Interpretación del Campo structureValidation

El frontend (cuando lo implementes) puede usar este campo para:

### 1. Mostrar indicador visual

```
Calidad del Prompt: ⭐⭐⭐⭐⭐ Excelente (100%)

Secciones presentes:
✅ [ROL]
✅ [TAREA]
✅ [AUDIENCIA]
✅ [FORMATO]
✅ [CONTEXTO]
```

### 2. Alertas si está incompleto

```
⚠️ Atención: Este prompt tiene calidad "Mejorable" (60%)

Secciones faltantes:
❌ [FORMATO]
❌ [CONTEXTO]

Recomendación: Revisar manualmente o regenerar
```

### 3. Badge de calidad

```
[Excelente] - Verde
[Buena] - Amarillo
[Mejorable] - Naranja
[Incompleta] - Rojo
```

---

## Casos de Prueba

### Caso 1: Prompt completo (100%)
```json
{
  "structureValidation": {
    "hasRol": true,
    "hasTarea": true,
    "hasAudiencia": true,
    "hasFormato": true,
    "hasContexto": true,
    "completeness": 100,
    "quality": "Excelente"
  }
}
```

### Caso 2: Prompt parcial (80%)
```json
{
  "structureValidation": {
    "hasRol": true,
    "hasTarea": true,
    "hasAudiencia": true,
    "hasFormato": true,
    "hasContexto": false,
    "completeness": 80,
    "quality": "Buena"
  }
}
```

### Caso 3: Prompt incompleto (40%)
```json
{
  "structureValidation": {
    "hasRol": true,
    "hasTarea": true,
    "hasAudiencia": false,
    "hasFormato": false,
    "hasContexto": false,
    "completeness": 40,
    "quality": "Incompleta"
  }
}
```

---

## Logs Esperados

Con validación completa:
```
INFO  c.p.service.AIEnhancementService : ✓ Prompt mejorado con estructura completa (100%): ROL, TAREA, AUDIENCIA, FORMATO, CONTEXTO
```

Con estructura parcial:
```
WARN  c.p.service.AIEnhancementService : ⚠ Estructura parcial (60%): Faltan secciones
WARN  c.p.service.AIEnhancementService :   - Falta [FORMATO]
WARN  c.p.service.AIEnhancementService :   - Falta [CONTEXTO]
```

---

## Beneficios

✅ **Transparencia**: El usuario ve qué secciones están presentes
✅ **Calidad visible**: Badge de "Excelente", "Buena", etc.
✅ **Debugging**: Los logs ayudan a identificar problemas
✅ **Educativo**: El usuario aprende qué hace falta un prompt completo
✅ **Frontend-ready**: Campo listo para visualización en UI

---

## Notas

- La validación es automática y no requiere configuración
- Groq/Llama 3.3 normalmente genera las 5 secciones correctamente
- Si falta alguna sección, se añade advertencia automática
- El porcentaje de completitud es útil para métricas
