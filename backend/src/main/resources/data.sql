-- Seed data para Skills predefinidas

-- 1. Code Reviewer Expert
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Code Reviewer Expert', 'code-quality', 
'Revisa código con enfoque profesional en seguridad, performance y best practices',
$$[ROL]
Actúa como Senior Code Reviewer con {years_experience} años de experiencia en {technology_stack}.

[TAREA]
Revisa el siguiente código enfocándote en:
{review_aspects}

CÓDIGO A REVISAR:
```
{code}
```

[AUDIENCIA]
Desarrolladores que necesitan feedback constructivo y accionable.

[FORMATO]
{output_format}

[CONTEXTO/DETALLES]
- Tecnología: {technology_stack}
- Nivel del equipo: {team_level}
- Prioridad: {priority_focus}$$,
$$[
  {"name": "technology_stack", "type": "select", "description": "Stack tecnológico", "options": ["Java/Spring Boot", "JavaScript/React", "Python/Django", "TypeScript/Node.js"], "required": true},
  {"name": "years_experience", "type": "select", "options": ["5", "10", "15", "20+"], "defaultValue": "10", "required": true},
  {"name": "review_aspects", "type": "multiselect", "options": ["Seguridad", "Performance", "Legibilidad", "Testing", "Arquitectura"], "defaultValue": "Seguridad,Performance", "required": true},
  {"name": "team_level", "type": "select", "options": ["Junior", "Mid", "Senior"], "defaultValue": "Mid", "required": true},
  {"name": "priority_focus", "type": "select", "options": ["Seguridad", "Performance", "Mantenibilidad"], "defaultValue": "Seguridad", "required": true},
  {"name": "output_format", "type": "select", "options": ["Lista detallada con ejemplos", "Tabla comparativa", "Código corregido con comentarios"], "defaultValue": "Lista detallada con ejemplos", "required": true},
  {"name": "code", "type": "text", "description": "Código a revisar", "required": true}
]$$,
$$### Issues Encontrados
1. [CRITICAL] SQL Injection vulnerability en línea 45...$$,
',code-review,quality,security,',
0,
'intermediate', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 2. Test Generator Pro
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Test Generator Pro', 'testing',
'Genera tests unitarios completos con cobertura exhaustiva',
$$[ROL]
Actúa como QA Engineer especializado en {testing_framework}.

[TAREA]
Genera tests unitarios para esta función/método:

```{language}
{code_to_test}
```

Incluye tests para:
1. Happy path (casos normales)
2. Edge cases (límites, valores extremos)
3. Error handling (excepciones, validaciones)
4. {additional_scenarios}

[AUDIENCIA]
Desarrolladores que practican TDD y buscan alta cobertura.

[FORMATO]
- Tests con nombres descriptivos
- Arrange-Act-Assert pattern
- Mocks apropiados
- Assertions claras

[CONTEXTO/DETALLES]
- Framework: {testing_framework}
- Lenguaje: {language}
- Cobertura objetivo: {coverage_target}%$$,
$$[
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "TypeScript"], "required": true},
  {"name": "testing_framework", "type": "select", "options": ["JUnit 5", "Jest", "pytest", "Mocha"], "required": true},
  {"name": "coverage_target", "type": "select", "options": ["70", "80", "90", "100"], "defaultValue": "80", "required": true},
  {"name": "additional_scenarios", "type": "text", "description": "Escenarios adicionales a testear", "required": false},
  {"name": "code_to_test", "type": "text", "description": "Código a testear", "required": true}
]$$,
$$@Test
void shouldCalculateDiscount_whenValidInput() {...}$$,
',testing,tdd,quality,',
0,
'intermediate', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 3. API Documentation Writer
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('API Documentation Writer', 'documentation',
'Genera documentación completa y profesional para APIs REST',
$$[ROL]
Actúa como Technical Writer especializado en documentación de APIs.

[TAREA]
Documenta el siguiente endpoint REST:

METHOD: {http_method}
PATH: {endpoint_path}
DESCRIPCIÓN: {endpoint_description}

[AUDIENCIA]
{audience_type}

[FORMATO]
Genera documentación en formato {doc_format} que incluya:
- Descripción clara del endpoint
- Parámetros de entrada con tipos y validaciones
- Ejemplos de requests
- Ejemplos de responses (éxito y errores)
- Códigos de estado HTTP posibles
- {additional_sections}

[CONTEXTO/DETALLES]
- Autenticación: {auth_type}
- Rate limiting: {rate_limit}
- Versión API: {api_version}$$,
$$[
  {"name": "http_method", "type": "select", "options": ["GET", "POST", "PUT", "DELETE", "PATCH"], "required": true},
  {"name": "endpoint_path", "type": "text", "description": "Ruta del endpoint (ej: /api/users/{id})", "required": true},
  {"name": "endpoint_description", "type": "text", "description": "Qué hace el endpoint", "required": true},
  {"name": "audience_type", "type": "select", "options": ["Desarrolladores externos", "Equipo interno", "Clientes técnicos"], "defaultValue": "Desarrolladores externos", "required": true},
  {"name": "doc_format", "type": "select", "options": ["OpenAPI/Swagger", "Markdown", "Postman Collection"], "defaultValue": "Markdown", "required": true},
  {"name": "auth_type", "type": "select", "options": ["Bearer Token", "API Key", "OAuth2", "None"], "defaultValue": "Bearer Token", "required": true},
  {"name": "rate_limit", "type": "text", "defaultValue": "100 requests/hour", "required": false},
  {"name": "api_version", "type": "text", "defaultValue": "v1", "required": false},
  {"name": "additional_sections", "type": "text", "description": "Secciones adicionales a incluir", "required": false}
]$$,
$$## GET /api/users/{id}
Obtiene información de un usuario...$$,
',documentation,api,rest,',
0,
'beginner', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 4. Refactoring Assistant
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Refactoring Assistant', 'refactoring',
'Sugiere mejoras de código aplicando principios SOLID y clean code',
$$[ROL]
Actúa como Software Architect especializado en clean code y principios SOLID.

[TAREA]
Analiza este código y propón refactorings:

```{language}
{code_to_refactor}
```

Enfócate en:
{refactoring_focus}

[AUDIENCIA]
Desarrolladores que buscan mejorar la calidad y mantenibilidad del código.

[FORMATO]
Para cada refactoring propuesto:
1. Identificar el code smell o problema
2. Explicar por qué es un problema
3. Mostrar el código refactorizado
4. Beneficios de la mejora

[CONTEXTO/DETALLES]
- Lenguaje: {language}
- Principios a aplicar: {principles}
- Nivel de agresividad: {aggressiveness}$$,
$$[
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "C#", "TypeScript"], "required": true},
  {"name": "refactoring_focus", "type": "multiselect", "options": ["Extract Method", "Remove Duplication", "Simplify Conditionals", "Improve Names", "Apply Design Patterns"], "required": true},
  {"name": "principles", "type": "multiselect", "options": ["SOLID", "DRY", "KISS", "YAGNI", "Clean Code"], "defaultValue": "SOLID,DRY", "required": true},
  {"name": "aggressiveness", "type": "select", "options": ["Conservador", "Moderado", "Agresivo"], "defaultValue": "Moderado", "description": "Qué tan drásticos son los cambios", "required": true},
  {"name": "code_to_refactor", "type": "text", "description": "Código a refactorizar", "required": true}
]$$,
$$### Refactoring 1: Extract Method
PROBLEMA: Método demasiado largo (50 líneas)...$$,
',refactoring,clean-code,solid,',
0,
'advanced', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 5. Bug Hunter
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Bug Hunter', 'debugging',
'Identifica y diagnostica bugs de forma sistemática',
$$[ROL]
Actúa como Expert Debugger con {years_experience} años encontrando bugs complejos en {technology}.

[TAREA]
Analiza este código que presenta el siguiente comportamiento inesperado:

SÍNTOMA: {bug_symptom}

CÓDIGO:
```{language}
{buggy_code}
```

CONTEXTO:
{execution_context}

Realiza:
1. Análisis sistemático del código
2. Hipótesis sobre la causa
3. Identificación del bug
4. Solución propuesta
5. Cómo prevenir bugs similares

[AUDIENCIA]
Desarrollador que necesita entender no solo la solución sino el proceso de debugging.

[FORMATO]
{output_style}

[CONTEXTO/DETALLES]
- Tecnología: {technology}
- Severidad: {severity}
- Frecuencia: {frequency}$$,
$$[
  {"name": "technology", "type": "select", "options": ["Spring Boot", "React", "Node.js", "Django", "Angular"], "required": true},
  {"name": "language", "type": "select", "options": ["Java", "JavaScript", "Python", "TypeScript"], "required": true},
  {"name": "years_experience", "type": "select", "options": ["5", "10", "15"], "defaultValue": "10", "required": true},
  {"name": "bug_symptom", "type": "text", "description": "Qué comportamiento incorrecto se observa", "required": true},
  {"name": "execution_context", "type": "text", "description": "Cuándo/cómo ocurre el bug", "required": true},
  {"name": "severity", "type": "select", "options": ["Critical", "High", "Medium", "Low"], "defaultValue": "High", "required": true},
  {"name": "frequency", "type": "select", "options": ["Siempre", "Frecuente", "Ocasional", "Raro"], "required": true},
  {"name": "output_style", "type": "select", "options": ["Paso a paso detallado", "Tabla diagnóstico", "Narrativo"], "defaultValue": "Paso a paso detallado", "required": true},
  {"name": "buggy_code", "type": "text", "description": "Código con el bug", "required": true}
]$$,
$$### Análisis del Bug
HIPÓTESIS: Race condition en operación async...$$,
',debugging,troubleshooting,analysis,',
0,
'advanced', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());