# ISSUE 24: Mejorar Servicio /improve con Estructura Profesional

Actualiza el servicio de mejora de prompts para usar una estructura profesional con 5 secciones: ROL, TAREA, AUDIENCIA, FORMATO y CONTEXTO.

## Contexto

Actualmente, el servicio `/improve` genera prompts mejorados pero sin una estructura consistente.

**Nueva estructura a implementar**:

```
[ROL] 
Describe quién debe responder o desde qué perspectiva debe hacerlo. "Actúa como..."

[TAREA]
Explica claramente qué acción debe realizar el modelo

[AUDIENCIA]
Indica a quién va dirigido el resultado final

[FORMATO]
Define cómo debe presentarse la salida (el modelo elige el más útil según la tarea)

[CONTEXTO/DETALLES]
Agrega información relevante para mejorar la precisión
```

Esta estructura es estándar en prompt engineering profesional y mejora significativamente la calidad de las respuestas.

---

## Archivo a Modificar

**Ruta**: `backend/src/main/java/com/promptvault/service/AIEnhancementService.java`

---

## Cambio 1: Actualizar método `buildImprovementPrompt`

**REEMPLAZAR** el método completo `buildImprovementPrompt` por esta nueva versión:

```java
/**
 * Construye el prompt que se enviará a Groq para mejorar el prompt original.
 * 
 * Aplica estructura profesional de 5 secciones:
 * - ROL: Perspectiva desde la que debe responder
 * - TAREA: Acción específica a realizar
 * - AUDIENCIA: Destinatario del resultado
 * - FORMATO: Cómo presentar la salida
 * - CONTEXTO: Información relevante adicional
 */
private String buildImprovementPrompt(String originalContent, String category) {
    return String.format("""
        Eres un experto en prompt engineering con años de experiencia optimizando prompts para LLMs.
        
        Tu tarea es transformar el siguiente prompt en uno profesional usando la estructura de 5 secciones.
        
        PROMPT ORIGINAL (categoría: %s):
        ```
        %s
        ```
        
        ESTRUCTURA REQUERIDA:
        Transforma este prompt aplicando estas 5 secciones obligatorias:
        
        [ROL]
        Define quién debe responder o desde qué perspectiva:
        - Si es código: "Actúa como desarrollador senior especializado en..."
        - Si es debugging: "Actúa como experto en debugging de..."
        - Si es documentación: "Actúa como technical writer..."
        - Adapta el rol según la categoría y contenido
        
        [TAREA]
        Explica de forma clara y específica qué debe hacer el modelo:
        - Usa verbos de acción precisos
        - Divide tareas complejas en pasos numerados
        - Sé específico sobre el resultado esperado
        
        [AUDIENCIA]
        Indica para quién es el resultado:
        - Desarrolladores junior/senior
        - Usuarios técnicos/no técnicos
        - Stakeholders
        - Adapta según el contexto del prompt
        
        [FORMATO]
        Define cómo presentar la salida (elige el más apropiado):
        - Código comentado
        - Lista de pasos
        - Tabla comparativa
        - Markdown estructurado
        - JSON
        - Diagrama textual
        - Explicación narrativa
        IMPORTANTE: Elige TÚ el formato más útil según la tarea
        
        [CONTEXTO/DETALLES]
        Incluye información relevante:
        - Tecnologías específicas (versiones, frameworks)
        - Restricciones o limitaciones
        - Ejemplos si son necesarios
        - Mejores prácticas aplicables
        
        REGLAS ADICIONALES:
        1. Cada sección debe estar claramente marcada con su nombre entre corchetes: [ROL], [TAREA], etc.
        2. Usa un lenguaje preciso y sin ambigüedades
        3. Si el prompt original ya es bueno en algún aspecto, consérvalo
        4. Añade detalles técnicos relevantes según la categoría
        5. El prompt mejorado debe ser actionable (que se pueda ejecutar directamente)
        
        FORMATO DE RESPUESTA:
        Devuelve SOLO dos bloques separados por "---MEJORAS---":
        
        BLOQUE 1 - El prompt mejorado con las 5 secciones (sin introducción ni explicación)
        ---MEJORAS---
        BLOQUE 2 - Lista de mejoras realizadas (una por línea, empezando con "- ")
        
        EJEMPLO DE FORMATO:
        
        [ROL]
        Actúa como desarrollador senior...
        
        [TAREA]
        1. Analiza el código...
        2. Identifica el problema...
        
        [AUDIENCIA]
        Este análisis es para desarrolladores junior...
        
        [FORMATO]
        - Código corregido con comentarios explicativos
        - Explicación paso a paso de la solución
        
        [CONTEXTO/DETALLES]
        - Framework: Spring Boot 3.2
        - Java 17
        - Mejores prácticas: usar constructor injection...
        
        ---MEJORAS---
        - Añadida sección [ROL] definiendo perspectiva de experto
        - Estructurada [TAREA] en pasos numerados
        - Especificada [AUDIENCIA] para desarrolladores junior
        - Definido [FORMATO] de salida apropiado
        - Incluido [CONTEXTO] técnico relevante
        
        IMPORTANTE: No añadas ningún texto antes del prompt mejorado ni después de la lista de mejoras.
        Empieza directamente con [ROL] y termina con la última mejora.
        """, category, originalContent);
}
```

---

## Cambio 2: Actualizar método `parseImprovementResponse`

**ACTUALIZAR** el método `parseImprovementResponse` para validar que la respuesta contenga las 5 secciones:

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
    
    // Validar que contiene las 5 secciones requeridas
    boolean hasAllSections = 
        improvedContent.contains("[ROL]") &&
        improvedContent.contains("[TAREA]") &&
        improvedContent.contains("[AUDIENCIA]") &&
        improvedContent.contains("[FORMATO]") &&
        improvedContent.contains("[CONTEXTO");  // Acepta [CONTEXTO] o [CONTEXTO/DETALLES]
    
    if (!hasAllSections) {
        log.warn("El prompt mejorado no contiene todas las secciones requeridas. Mejorando...");
        
        // Si faltan secciones, añadir nota al principio
        String note = """
            NOTA: Este prompt mejorado puede no incluir todas las secciones estándar.
            Se recomienda revisar manualmente para mayor efectividad.
            
            """;
        improvedContent = note + improvedContent;
        
        // Añadir advertencia en mejoras
        List<String> updatedImprovements = new ArrayList<>(improvements);
        updatedImprovements.add(0, "⚠️ Estructura parcial: puede requerir refinamiento manual");
        improvements = updatedImprovements;
    } else {
        log.info("Prompt mejorado exitosamente con las 5 secciones: ROL, TAREA, AUDIENCIA, FORMATO, CONTEXTO");
    }
    
    return AIImprovementResponse.builder()
        .originalContent(originalContent)
        .improvedContent(improvedContent)
        .improvements(improvements)
        .tokenUsage(null)
        .build();
}
```

**AÑADIR import** al inicio del archivo (si no existe):

```java
import java.util.ArrayList;
```

---

## Verificación

### Compilar
```bash
cd backend
mvn clean compile
```

Debe compilar sin errores.

### Ejecutar
```bash
mvn spring-boot:run
```

### Probar en Swagger

1. Abrir: http://localhost:8080/swagger-ui.html
2. POST /api/prompts/1/improve
3. Ejecutar

**Respuesta esperada** (ejemplo):

```json
{
  "originalContent": "Analiza el siguiente código Java y encuentra el bug...",
  "improvedContent": "[ROL]\nActúa como desarrollador senior especializado en Java y Spring Boot con 10+ años de experiencia en debugging de aplicaciones empresariales.\n\n[TAREA]\n1. Analiza el código Java proporcionado línea por línea\n2. Identifica el bug o error presente\n3. Explica la causa raíz del problema\n4. Proporciona el código corregido\n5. Sugiere cómo prevenir este tipo de errores en el futuro\n\n[AUDIENCIA]\nDesarrolladores Java de nivel intermedio que necesitan entender no solo la solución, sino también el razonamiento detrás de ella.\n\n[FORMATO]\n- Código original con el error señalado\n- Explicación detallada del problema\n- Código corregido con comentarios explicativos\n- Lista de mejores prácticas aplicables\n- Ejemplo de test unitario para verificar la corrección\n\n[CONTEXTO/DETALLES]\n- Framework: Spring Boot 3.x\n- Lenguaje: Java 17\n- Patrón a seguir: Clean Code principles\n- Considera aspectos de: performance, seguridad, mantenibilidad\n- Incluye validación de null-safety donde sea relevante",
  "improvements": [
    "Añadida sección [ROL] definiendo expertise específico en Java/Spring Boot",
    "Estructurada [TAREA] en 5 pasos claros y ordenados",
    "Especificada [AUDIENCIA] para desarrolladores de nivel intermedio",
    "Definido [FORMATO] multi-modal (código + explicación + test)",
    "Incluido [CONTEXTO] técnico con versiones y mejores prácticas"
  ]
}
```

---

## Ejemplo de Uso Real

### Antes (prompt simple):
```
"Analiza este código y encuentra el bug"
```

### Después (prompt mejorado):
```
[ROL]
Actúa como desarrollador senior especializado en Java...

[TAREA]
1. Analiza el código...
2. Identifica el bug...
3. Explica la causa...

[AUDIENCIA]
Desarrolladores Java nivel intermedio...

[FORMATO]
- Código original con error señalado
- Explicación detallada
- Código corregido con comentarios
- Mejores prácticas

[CONTEXTO/DETALLES]
- Framework: Spring Boot 3.x
- Java 17
- Clean Code principles
```

---

## Notas Técnicas

- **Groq (Llama 3.3)** entiende muy bien esta estructura de 5 secciones
- El modelo elige automáticamente el FORMATO más apropiado
- La validación asegura que todas las secciones estén presentes
- Si falta alguna sección, se añade una advertencia pero no falla
- Los logs indican si la estructura está completa

---

## Beneficios de esta Mejora

✅ **Prompts más profesionales**: Estructura estándar de la industria
✅ **Mayor claridad**: Cada aspecto del prompt está bien definido
✅ **Mejores resultados**: Los LLMs responden mejor a prompts estructurados
✅ **Reutilizable**: Los prompts mejorados son más fáciles de adaptar
✅ **Educativo**: Los usuarios aprenden a escribir mejores prompts

---

## Testing

Probar con diferentes categorías:

1. **Debugging**: Debe generar ROL de "experto en debugging"
2. **Code Generation**: Debe generar ROL de "desarrollador senior"
3. **Testing**: Debe generar ROL de "QA engineer o test architect"
4. **Documentation**: Debe generar ROL de "technical writer"
5. **Refactoring**: Debe generar ROL de "arquitecto de software"

El FORMATO debe adaptarse automáticamente a cada tipo de tarea.
