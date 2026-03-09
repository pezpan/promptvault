-- Seed data para Categorías
INSERT INTO categories (name, description, icon, color) VALUES
('code-generation', 'Prompts para generar codigo', 'code', '#4CAF50'),
('debugging', 'Prompts para encontrar y corregir bugs', 'bug', '#F44336'),
('refactoring', 'Prompts para mejorar codigo existente', 'refresh', '#2196F3'),
('testing', 'Prompts para generar tests unitarios', 'check', '#FF9800'),
('documentation', 'Prompts para generar documentacion', 'book', '#9C27B0');

-- Seed data para Prompts de ejemplo
INSERT INTO prompts (title, description, content, category, tags, is_favorite, usage_count, status, created_at, updated_at) VALUES
('Java Bug Fixer', 'Analiza y corrige bugs en codigo Java', 'Analiza el siguiente codigo Java y encuentra el bug:

CONTEXTO:
- Framework: Spring Boot
- Lenguaje: Java 17

CODIGO:
{pegar codigo aqui}

TAREAS:
1. Identifica la causa raiz del bug
2. Propone una solucion con explicacion
3. Muestra el codigo corregido
4. Sugiere como prevenir bugs similares', 'debugging', ',java,spring-boot,bug-fix,', true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('React Component Generator', 'Genera componentes React funcionales', 'Crea un componente React funcional para {descripcion}:

REQUISITOS:
- React 18 con hooks
- TypeScript
- Styled con Tailwind CSS
- Props con interfaces TypeScript
- Manejo de estado apropiado

INCLUYE:
- Prop validation
- Default props
- JSDoc comments', 'code-generation', ',react,typescript,tailwind,', true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('Unit Test Generator', 'Genera tests unitarios completos', 'Genera tests unitarios para esta funcion:

CODIGO:
{pegar funcion}

FRAMEWORK: {JUnit 5 / Jest / pytest}

GENERA:
- Tests para happy path
- Tests para casos edge
- Tests para manejo de errores
- Mocks apropiados
- Assertions claras', 'testing', ',testing,tdd,unit-tests,', false, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Seed data para MCP Servers populares
INSERT INTO mcp_servers (name, description, category, tags, command, args, env_vars, capabilities, documentation, official_url, installation_instructions, config_example, usage_count, verified, created_at, updated_at) VALUES
('GitHub', 'Acceso completo a repositorios, issues, PRs y busqueda de codigo en GitHub', 'development', ',github,git,code,repositories,', 'npx', '["-y", "@modelcontextprotocol/server-github"]', '{"GITHUB_TOKEN": "<your_github_token>"}', '["read_repos", "create_issues", "search_code", "manage_prs"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github', 'Requiere Node.js instalado. Generar token en GitHub Settings > Developer settings > Personal access tokens', '{"mcpServers": {"github": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-github"], "env": {"GITHUB_TOKEN": "ghp_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Filesystem', 'Leer y escribir archivos en el sistema local', 'filesystem', ',files,local,storage,', 'npx', '["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/directory"]', '{}', '["read_file", "write_file", "list_directory", "create_directory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem', 'Especifica el directorio permitido como argumento. Solo puede acceder a ese directorio y subdirectorios', '{"mcpServers": {"filesystem": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/username/Documents"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('PostgreSQL', 'Ejecutar consultas SQL en bases de datos PostgreSQL', 'database', ',postgresql,sql,database,', 'npx', '["-y", "@modelcontextprotocol/server-postgres"]', '{"POSTGRES_URL": "postgresql://user:pass@localhost:5432/db"}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/postgres', 'Requiere PostgreSQL instalado y accesible. Proporciona URL de conexion', '{"mcpServers": {"postgres": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-postgres"], "env": {"POSTGRES_URL": "postgresql://localhost/mydb"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Slack', 'Enviar mensajes, leer canales y gestionar workspace de Slack', 'productivity', ',slack,messaging,collaboration,', 'npx', '["-y", "@modelcontextprotocol/server-slack"]', '{"SLACK_BOT_TOKEN": "xoxb-your-token", "SLACK_TEAM_ID": "T01234"}', '["send_message", "read_channel", "list_channels", "user_info"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/slack', 'Crear Slack App en api.slack.com con permisos necesarios. Instalar en workspace y copiar Bot Token', '{"mcpServers": {"slack": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-slack"], "env": {"SLACK_BOT_TOKEN": "xoxb-xxx", "SLACK_TEAM_ID": "T01234"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Brave Search', 'Busqueda web usando Brave Search API', 'search', ',search,web,brave,', 'npx', '["-y", "@modelcontextprotocol/server-brave-search"]', '{"BRAVE_API_KEY": "<your_api_key>"}', '["web_search", "local_search", "news_search"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search', 'Obtener API key gratuita en brave.com/search/api', '{"mcpServers": {"brave": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-brave-search"], "env": {"BRAVE_API_KEY": "BSA_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Memory', 'Sistema de memoria persistente para recordar informacion entre sesiones', 'utility', ',memory,storage,context,', 'npx', '["-y", "@modelcontextprotocol/server-memory"]', '{}', '["store_memory", "retrieve_memory", "search_memory", "delete_memory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/memory', 'No requiere configuracion adicional. Almacena informacion en archivo local', '{"mcpServers": {"memory": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-memory"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Seed data para Skills predefinidas

-- 1. Code Reviewer Expert
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Code Reviewer Expert', 'code-quality', 
'Revisa codigo con enfoque profesional en seguridad, performance y best practices',
'[ROL]
Actua como Senior Code Reviewer con {years_experience} anos de experiencia en {technology_stack}.

[TAREA]
Revisa el siguiente codigo enfocandote en:
{review_aspects}

CODIGO A REVISAR:
```
{code}
```

[AUDIENCIA]
Desarrolladores que necesitan feedback constructivo y accionable.

[FORMATO]
{output_format}

[CONTEXTO/DETALLES]
- Tecnologia: {technology_stack}
- Nivel del equipo: {team_level}
- Prioridad: {priority_focus}',
'[{\"name\": \"technology_stack\", \"type\": \"select\", \"description\": \"Stack tecnologico\", \"options\": [\"Java/Spring Boot\", \"JavaScript/React\", \"Python/Django\", \"TypeScript/Node.js\"], \"required\": true}, {\"name\": \"years_experience\", \"type\": \"select\", \"options\": [\"5\", \"10\", \"15\", \"20+\"], \"defaultValue\": \"10\", \"required\": true}, {\"name\": \"review_aspects\", \"type\": \"multiselect\", \"options\": [\"Seguridad\", \"Performance\", \"Legibilidad\", \"Testing\", \"Arquitectura\"], \"defaultValue\": \"Seguridad,Performance\", \"required\": true}, {\"name\": \"team_level\", \"type\": \"select\", \"options\": [\"Junior\", \"Mid\", \"Senior\"], \"defaultValue\": \"Mid\", \"required\": true}, {\"name\": \"priority_focus\", \"type\": \"select\", \"options\": [\"Seguridad\", \"Performance\", \"Mantenibilidad\"], \"defaultValue\": \"Seguridad\", \"required\": true}, {\"name\": \"output_format\", \"type\": \"select\", \"options\": [\"Lista detallada con ejemplos\", \"Tabla comparativa\", \"Codigo corregido con comentarios\"], \"defaultValue\": \"Lista detallada con ejemplos\", \"required\": true}, {\"name\": \"code\", \"type\": \"text\", \"description\": \"Codigo a revisar\", \"required\": true}]',
'### Issues Encontrados
1. [CRITICAL] SQL Injection vulnerability en linea 45...',
',code-review,quality,security,', 0, 'intermediate', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 2. Test Generator Pro
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('Test Generator Pro', 'testing',
'Genera tests unitarios completos con cobertura exhaustiva',
'[ROL]
Actua como QA Engineer especializado en {testing_framework}.

[TAREA]
Genera tests unitarios para esta funcion/metodo:

```{language}
{code_to_test}
```

Incluye tests para:
1. Happy path (casos normales)
2. Edge cases (limites, valores extremos)
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
- Cobertura objetivo: {coverage_target}%',
'[{\"name\": \"language\", \"type\": \"select\", \"options\": [\"Java\", \"JavaScript\", \"Python\", \"TypeScript\"], \"required\": true}, {\"name\": \"testing_framework\", \"type\": \"select\", \"options\": [\"JUnit 5\", \"Jest\", \"pytest\", \"Mocha\"], \"required\": true}, {\"name\": \"coverage_target\", \"type\": \"select\", \"options\": [\"70\", \"80\", \"90\", \"100\"], \"defaultValue\": \"80\", \"required\": true}, {\"name\": \"additional_scenarios\", \"type\": \"text\", \"description\": \"Escenarios adicionales a testear\", \"required\": false}, {\"name\": \"code_to_test\", \"type\": \"text\", \"description\": \"Codigo a testear\", \"required\": true}]',
'@Test
void shouldCalculateDiscount_whenValidInput() {...}',
',testing,tdd,quality,', 0, 'intermediate', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 3. API Documentation Writer
INSERT INTO skills (name, category, description, prompt_template, parameters, example_output, tags, usage_count, difficulty_level, created_at, updated_at) VALUES
('API Documentation Writer', 'documentation',
'Genera documentacion completa y profesional para APIs REST',
'[ROL]
Actua como Technical Writer especializado en documentacion de APIs.

[TAREA]
Documenta el siguiente endpoint REST:

METHOD: {http_method}
PATH: {endpoint_path}
DESCRIPCION: {endpoint_description}

[AUDIENCIA]
{audience_type}

[FORMATO]
Genera documentacion en formato {doc_format} que incluya:
- Descripcion clara del endpoint
- Parametros de entrada con tipos y validaciones
- Ejemplos de requests
- Ejemplos de responses (exito y errores)
- Codigos de estado HTTP posibles
- {additional_sections}

[CONTEXTO/DETALLES]
- Autenticacion: {auth_type}
- Rate limiting: {rate_limit}
- Version API: {api_version}',
'[{\"name\": \"http_method\", \"type\": \"select\", \"options\": [\"GET\", \"POST\", \"PUT\", \"DELETE\", \"PATCH\"], \"required\": true}, {\"name\": \"endpoint_path\", \"type\": \"text\", \"description\": \"Ruta del endpoint (ej: /api/users/{id})\", \"required\": true}, {\"name\": \"endpoint_description\", \"type\": \"text\", \"description\": \"Que hace el endpoint\", \"required\": true}, {\"name\": \"audience_type\", \"type\": \"select\", \"options\": [\"Desarrolladores externos\", \"Equipo interno\", \"Clientes tecnicos\"], \"defaultValue\": \"Desarrolladores externos\", \"required\": true}, {\"name\": \"doc_format\", \"type\": \"select\", \"options\": [\"OpenAPI/Swagger\", \"Markdown\", \"Postman Collection\"], \"defaultValue\": \"Markdown\", \"required\": true}, {\"name\": \"auth_type\", \"type\": \"select\", \"options\": [\"Bearer Token\", \"API Key\", \"OAuth2\", \"None\"], \"defaultValue\": \"Bearer Token\", \"required\": true}, {\"name\": \"rate_limit\", \"type\": \"text\", \"defaultValue\": \"100 requests/hour\", \"required\": false}, {\"name\": \"api_version\", \"type\": \"text\", \"defaultValue\": \"v1\", \"required\": false}, {\"name\": \"additional_sections\", \"type\": \"text\", \"description\": \"Secciones adicionales a incluir\", \"required\": false}]',
'## GET /api/users/{id}
Obtiene informacion de un usuario...',
',documentation,api,rest,', 0, 'beginner', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
