# Issue 48: [SKILL-BUILDER] SkillBuilderController - Endpoint REST

## Objetivo
Añadir el endpoint de construcción de Skills al SkillController existente.

## Archivo a modificar
`src/main/java/com/promptvault/controller/SkillController.java`

## Cambios a añadir

### 1. Inyectar SkillBuilderService
```java
// Añadir a las dependencias del constructor/campo:
private final SkillBuilderService skillBuilderService;
```

### 2. Añadir endpoint
```java
@PostMapping("/build")
@Operation(
    summary = "Generar una nueva Skill con IA",
    description = """
        Usa Groq IA para generar una Skill completa a partir de una descripción en
        lenguaje natural. La IA crea automáticamente:
        - El template parametrizable con {{PARAMETROS}}
        - Los parámetros detectados y sus descripciones
        - Un ejemplo de output
        - Una puntuación estimada de calidad (0-100)
        
        **Ejemplo de request:**
        ```json
        {
          "objective": "Quiero una skill para revisar código Python buscando vulnerabilidades de seguridad OWASP Top 10",
          "targetAudience": "Desarrolladores Python con conocimientos de seguridad",
          "exampleInputs": ["def login(user, pwd): return db.query(f'SELECT * FROM users WHERE pwd={pwd}')"],
          "desiredOutputFormat": "Lista de vulnerabilidades con: severidad, CWE ID, descripción y código corregido",
          "category": "security",
          "saveToDatabase": false
        }
        ```
        
        **Nota:** Con `saveToDatabase: true` la skill se guarda directamente en BD
        y puede usarse inmediatamente con `POST /api/skills/{id}/generate`.
        """
)
public ResponseEntity<SkillBuildResult> buildSkill(
        @Valid @RequestBody SkillBuildRequest request) {
    return ResponseEntity.ok(skillBuilderService.buildSkill(request));
}
```

### 3. Imports necesarios
```java
import com.promptvault.dto.SkillBuildRequest;
import com.promptvault.dto.SkillBuildResult;
import com.promptvault.service.SkillBuilderService;
import jakarta.validation.Valid;
```

## Endpoint resultante

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | /api/skills/build | Generar skill con IA |

## Ejemplo de respuesta esperada

```json
{
  "skill": {
    "name": "Python Security Auditor",
    "description": "Analiza código Python identificando vulnerabilidades OWASP Top 10 con severidad y corrección",
    "template": "Actúa como un experto en seguridad de aplicaciones Python con conocimiento profundo de OWASP Top 10.\n\nANALIZA el siguiente código Python:\n```python\n{{CODE}}\n```\n\nCONTEXTO: {{CONTEXT}}\n\nIdentifica todas las vulnerabilidades presentes siguiendo este formato para cada una:\n\n## Vulnerabilidad [N]\n- **Severidad**: [CRÍTICA/ALTA/MEDIA/BAJA]\n- **CWE ID**: CWE-XXX\n- **Descripción**: Explicación de la vulnerabilidad\n- **Línea(s) afectada(s)**: [número]\n- **Código vulnerable**:\n```python\n[código problemático]\n```\n- **Corrección sugerida**:\n```python\n[código corregido]\n```\n\nAl final, proporciona un **resumen ejecutivo** con el nivel de riesgo global.",
    "parameters": ["CODE", "CONTEXT"],
    "parameterDescriptions": {
      "CODE": "El código Python a analizar (función, clase o módulo completo)",
      "CONTEXT": "Contexto del sistema: tipo de aplicación, datos que maneja, entorno de ejecución"
    },
    "exampleOutput": "## Vulnerabilidad 1\n- **Severidad**: CRÍTICA\n- **CWE ID**: CWE-89 (SQL Injection)\n...",
    "category": "security",
    "estimatedQualityScore": 88
  },
  "modelUsed": "llama-3.3-70b-versatile",
  "generationTimeMs": 2340,
  "savedSkillId": null
}
```

## Verificación en Swagger
1. Ir a http://localhost:8080/swagger-ui.html → sección "Skills"
2. Ejecutar `POST /api/skills/build` con el ejemplo del objetivo
3. Verificar que el template contiene `{{PARAMETROS}}`
4. Repetir con `saveToDatabase: true` y verificar que aparece en `GET /api/skills`
