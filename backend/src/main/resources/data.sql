-- Seed data para Categorías
INSERT INTO categories (name, description, icon, color) VALUES
('code-generation', 'Prompts para generar código', 'code', '#4CAF50'),
('debugging', 'Prompts para encontrar y corregir bugs', 'bug', '#F44336'),
('refactoring', 'Prompts para mejorar código existente', 'refresh', '#2196F3'),
('testing', 'Prompts para generar tests unitarios', 'check', '#FF9800'),
('documentation', 'Prompts para generar documentación', 'book', '#9C27B0');

-- Seed data para Prompts de ejemplo
INSERT INTO prompts (title, description, content, category, tags, is_favorite, usage_count, status, created_at, updated_at) VALUES
('Java Bug Fixer', 'Analiza y corrige bugs en código Java', 'Analiza el siguiente código Java y encuentra el bug:

CONTEXTO:
- Framework: Spring Boot
- Lenguaje: Java 17

CÓDIGO:
{pegar código aquí}

TAREAS:
1. Identifica la causa raíz del bug
2. Propón una solución con explicación
3. Muestra el código corregido
4. Sugiere cómo prevenir bugs similares', 'debugging', 'java,spring-boot,bug-fix', true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('React Component Generator', 'Genera componentes React funcionales', 'Crea un componente React funcional para {descripción}:

REQUISITOS:
- React 18 con hooks
- TypeScript
- Styled con Tailwind CSS
- Props con interfaces TypeScript
- Manejo de estado apropiado

INCLUYE:
- Prop validation
- Default props
- JSDoc comments', 'code-generation', 'react,typescript,tailwind', true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('Unit Test Generator', 'Genera tests unitarios completos', 'Genera tests unitarios para esta función:

CÓDIGO:
{pegar función}

FRAMEWORK: {JUnit 5 / Jest / pytest}

GENERA:
- Tests para happy path
- Tests para casos edge
- Tests para manejo de errores
- Mocks apropiados
- Assertions claras', 'testing', 'testing,tdd,unit-tests', false, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Seed data para MCP Servers populares
INSERT INTO mcp_servers (name, description, category, tags, command, args, env_vars, capabilities, documentation, official_url, installation_instructions, config_example, usage_count, verified, created_at, updated_at) VALUES
('GitHub', 'Acceso completo a repositorios, issues, PRs y búsqueda de código en GitHub', 'development', 'github,git,code,repositories', 'npx', '[["-y", "@modelcontextprotocol/server-github"]]', '{"GITHUB_TOKEN": "<your_github_token>"}', '["read_repos", "create_issues", "search_code", "manage_prs"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github', 'Requiere Node.js instalado. Generar token en GitHub Settings > Developer settings > Personal access tokens', '{"mcpServers": {"github": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-github"], "env": {"GITHUB_TOKEN": "ghp_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Filesystem', 'Leer y escribir archivos en el sistema local', 'filesystem', 'files,local,storage', 'npx', '[["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/directory"]]', '{}', '["read_file", "write_file", "list_directory", "create_directory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem', 'Especifica el directorio permitido como argumento. Solo puede acceder a ese directorio y subdirectorios', '{"mcpServers": {"filesystem": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/username/Documents"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('PostgreSQL', 'Ejecutar consultas SQL en bases de datos PostgreSQL', 'database', 'postgresql,sql,database', 'npx', '[["-y", "@modelcontextprotocol/server-postgres"]]', '{"POSTGRES_URL": "postgresql://user:pass@localhost:5432/db"}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/postgres', 'Requiere PostgreSQL instalado y accesible. Proporciona URL de conexión', '{"mcpServers": {"postgres": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-postgres"], "env": {"POSTGRES_URL": "postgresql://localhost/mydb"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Slack', 'Enviar mensajes, leer canales y gestionar workspace de Slack', 'productivity', 'slack,messaging,collaboration', 'npx', '[["-y", "@modelcontextprotocol/server-slack"]]', '{"SLACK_BOT_TOKEN": "xoxb-your-token", "SLACK_TEAM_ID": "T01234"}', '["send_message", "read_channel", "list_channels", "user_info"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/slack', 'Crear Slack App en api.slack.com con permisos necesarios. Instalar en workspace y copiar Bot Token', '{"mcpServers": {"slack": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-slack"], "env": {"SLACK_BOT_TOKEN": "xoxb-xxx", "SLACK_TEAM_ID": "T01234"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Google Drive', 'Buscar, leer y gestionar archivos en Google Drive', 'productivity', 'google-drive,cloud-storage,files', 'npx', '[["-y", "@modelcontextprotocol/server-gdrive"]]', '{"GDRIVE_CLIENT_ID": "xxx", "GDRIVE_CLIENT_SECRET": "xxx", "GDRIVE_REFRESH_TOKEN": "xxx"}', '["search_files", "read_file", "create_file", "share_file"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/gdrive', 'Crear proyecto en Google Cloud Console, habilitar Drive API, crear credenciales OAuth', '{"mcpServers": {"gdrive": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-gdrive"], "env": {"GDRIVE_CLIENT_ID": "xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Brave Search', 'Búsqueda web usando Brave Search API', 'search', 'search,web,brave', 'npx', '[["-y", "@modelcontextprotocol/server-brave-search"]]', '{"BRAVE_API_KEY": "<your_api_key>"}', '["web_search", "local_search", "news_search"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search', 'Obtener API key gratuita en brave.com/search/api', '{"mcpServers": {"brave": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-brave-search"], "env": {"BRAVE_API_KEY": "BSA_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Memory', 'Sistema de memoria persistente para recordar información entre sesiones', 'utility', 'memory,storage,context', 'npx', '[["-y", "@modelcontextprotocol/server-memory"]]', '{}', '["store_memory", "retrieve_memory", "search_memory", "delete_memory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/memory', 'No requiere configuración adicional. Almacena información en archivo local', '{"mcpServers": {"memory": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-memory"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Puppeteer', 'Automatización de navegador web con Puppeteer', 'automation', 'puppeteer,browser,scraping,automation', 'npx', '[["-y", "@modelcontextprotocol/server-puppeteer"]]', '{}', '["navigate", "screenshot", "click", "type", "extract_content"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/puppeteer', 'Permite controlar Chrome/Chromium para web scraping y automatización', '{"mcpServers": {"puppeteer": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-puppeteer"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('SQLite', 'Consultar bases de datos SQLite locales', 'database', 'sqlite,database,sql', 'npx', '[["-y", "@modelcontextprotocol/server-sqlite", "/path/to/database.db"]]', '{}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite', 'Especifica la ruta al archivo .db como argumento', '{"mcpServers": {"sqlite": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-sqlite", "/data/app.db"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Fetch', 'Realizar peticiones HTTP/HTTPS para obtener contenido web', 'web', 'fetch,http,web,api', 'npx', '[["-y", "@modelcontextprotocol/server-fetch"]]', '{}', '["fetch_url", "fetch_html", "fetch_json"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/fetch', 'Permite obtener contenido de URLs. Útil para leer páginas web y APIs', '{"mcpServers": {"fetch": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-fetch"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

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