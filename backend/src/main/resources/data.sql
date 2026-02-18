-- Categorías iniciales
INSERT INTO categories (name, description, icon, color) VALUES
('code-generation', 'Prompts para generar código', 'code', '#4CAF50'),
('debugging', 'Prompts para encontrar y corregir bugs', 'bug', '#F44336'),
('refactoring', 'Prompts para mejorar código existente', 'refresh', '#2196F3'),
('testing', 'Prompts para generar tests unitarios', 'check', '#FF9800'),
('documentation', 'Prompts para generar documentación', 'book', '#9C27B0');

-- Prompts de ejemplo
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
4. Sugiere cómo prevenir bugs similares', 'debugging', (',java,spring-boot,bug-fix,'), true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

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
- JSDoc comments', 'code-generation', (',react,typescript,tailwind,'), true, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

('Unit Test Generator', 'Genera tests unitarios completos', 'Genera tests unitarios para esta función:

CÓDIGO:
{pegar función}

FRAMEWORK: {JUnit 5 / Jest / pytest}

GENERA:
- Tests para happy path
- Tests para casos edge
- Tests para manejo de errores
- Mocks apropiados
- Assertions claras', 'testing', (',testing,tdd,unit-tests,'), false, 0, 'published', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- Seed data para MCP Servers populares
INSERT INTO mcp_servers (name, description, category, tags, command, args, env_vars, capabilities, documentation, official_url, installation_instructions, config_example, usage_count, verified, created_at, updated_at) VALUES

-- 1. GitHub MCP Server
('GitHub', 'Acceso completo a repositorios, issues, PRs y búsqueda de código en GitHub', 'development', ',github,git,code,repositories,', 'npx', '[["-y", "@modelcontextprotocol/server-github"]]', '{"GITHUB_TOKEN": "<your_github_token>"}', '["read_repos", "create_issues", "search_code", "manage_prs"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/github', 'Requiere Node.js instalado. Generar token en GitHub Settings > Developer settings > Personal access tokens', '{"mcpServers": {"github": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-github"], "env": {"GITHUB_TOKEN": "ghp_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 2. Filesystem MCP Server
('Filesystem', 'Leer y escribir archivos en el sistema local', 'filesystem', ',files,local,storage,', 'npx', '[["-y", "@modelcontextprotocol/server-filesystem", "/path/to/allowed/directory"]]', '{}', '["read_file", "write_file", "list_directory", "create_directory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/filesystem', 'Especifica el directorio permitido como argumento. Solo puede acceder a ese directorio y subdirectorios', '{"mcpServers": {"filesystem": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/username/Documents"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 3. PostgreSQL MCP Server
('PostgreSQL', 'Ejecutar consultas SQL en bases de datos PostgreSQL', 'database', ',postgresql,sql,database,', 'npx', '[["-y", "@modelcontextprotocol/server-postgres"]]', '{"POSTGRES_URL": "postgresql://user:pass@localhost:5432/db"}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/postgres', 'Requiere PostgreSQL instalado y accesible. Proporciona URL de conexión', '{"mcpServers": {"postgres": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-postgres"], "env": {"POSTGRES_URL": "postgresql://localhost/mydb"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 4. Slack MCP Server
('Slack', 'Enviar mensajes, leer canales y gestionar workspace de Slack', 'productivity', ',slack,messaging,collaboration,', 'npx', '[["-y", "@modelcontextprotocol/server-slack"]]', '{"SLACK_BOT_TOKEN": "xoxb-your-token", "SLACK_TEAM_ID": "T01234"}', '["send_message", "read_channel", "list_channels", "user_info"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/slack', 'Crear Slack App en api.slack.com con permisos necesarios. Instalar en workspace y copiar Bot Token', '{"mcpServers": {"slack": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-slack"], "env": {"SLACK_BOT_TOKEN": "xoxb-xxx", "SLACK_TEAM_ID": "T01234"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 5. Google Drive MCP Server
('Google Drive', 'Buscar, leer y gestionar archivos en Google Drive', 'productivity', ',google-drive,cloud-storage,files,', 'npx', '[["-y", "@modelcontextprotocol/server-gdrive"]]', '{"GDRIVE_CLIENT_ID": "xxx", "GDRIVE_CLIENT_SECRET": "xxx", "GDRIVE_REFRESH_TOKEN": "xxx"}', '["search_files", "read_file", "create_file", "share_file"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/gdrive', 'Crear proyecto en Google Cloud Console, habilitar Drive API, crear credenciales OAuth', '{"mcpServers": {"gdrive": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-gdrive"], "env": {"GDRIVE_CLIENT_ID": "xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 6. Brave Search MCP Server
('Brave Search', 'Búsqueda web usando Brave Search API', 'search', ',search,web,brave,', 'npx', '[["-y", "@modelcontextprotocol/server-brave-search"]]', '{"BRAVE_API_KEY": "<your_api_key>"}', '["web_search", "local_search", "news_search"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/brave-search', 'Obtener API key gratuita en brave.com/search/api', '{"mcpServers": {"brave": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-brave-search"], "env": {"BRAVE_API_KEY": "BSA_xxx"}}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 7. Memory MCP Server
('Memory', 'Sistema de memoria persistente para recordar información entre sesiones', 'utility', ',memory,storage,context,', 'npx', '[["-y", "@modelcontextprotocol/server-memory"]]', '{}', '["store_memory", "retrieve_memory", "search_memory", "delete_memory"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/memory', 'No requiere configuración adicional. Almacena información en archivo local', '{"mcpServers": {"memory": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-memory"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 8. Puppeteer MCP Server
('Puppeteer', 'Automatización de navegador web con Puppeteer', 'automation', ',puppeteer,browser,scraping,automation,', 'npx', '[["-y", "@modelcontextprotocol/server-puppeteer"]]', '{}', '["navigate", "screenshot", "click", "type", "extract_content"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/puppeteer', 'Permite controlar Chrome/Chromium para web scraping y automatización', '{"mcpServers": {"puppeteer": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-puppeteer"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 9. SQLite MCP Server
('SQLite', 'Consultar bases de datos SQLite locales', 'database', ',sqlite,database,sql,', 'npx', '[["-y", "@modelcontextprotocol/server-sqlite", "/path/to/database.db"]]', '{}', '["read_tables", "execute_query", "describe_schema"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/sqlite', 'Especifica la ruta al archivo .db como argumento', '{"mcpServers": {"sqlite": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-sqlite", "/data/app.db"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),

-- 10. Fetch MCP Server
('Fetch', 'Realizar peticiones HTTP/HTTPS para obtener contenido web', 'web', ',fetch,http,web,api,', 'npx', '[["-y", "@modelcontextprotocol/server-fetch"]]', '{}', '["fetch_url", "fetch_html", "fetch_json"]', 'https://github.com/modelcontextprotocol/servers', 'https://github.com/modelcontextprotocol/servers/tree/main/src/fetch', 'Permite obtener contenido de URLs. Útil para leer páginas web y APIs', '{"mcpServers": {"fetch": {"command": "npx", "args": ["-y", "@modelcontextprotocol/server-fetch"]}}}', 0, true, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());