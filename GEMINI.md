# 🧠 Gemini CLI Context: PromptVault

## 📌 Proyecto: PromptVault
**PromptVault** es una plataforma de orquestación de agentes de IA de próxima generación. Gestiona prompts, construye "Skills" parametrizadas y orquesta "Workflows" complejos con contexto acumulativo, utilizando el protocolo MCP para la integración de herramientas externas.

**Versión Actual**: v1.1.0  
**Estado**: ✅ Stable - Ready for Production  
**Frontend**: AI Studio (React + TypeScript + Tailwind CSS)

---

## 🏗️ Arquitectura Técnica (Spring Boot 3 + Java 17)

### 1. Entidades de Dominio (Corazón del Sistema)
- **`Prompt`**: Unidad básica. Soporta versiones y mejoras sugeridas por IA.
- **`Skill`**: Plantillas parametrizadas (`{{param}}`). El `SkillBuilderService` genera estas plantillas automáticamente.
- **`MCPServer`**: Configuración para herramientas externas (Model Context Protocol). Soporta validación estática y de conectividad.
  - **NEW**: Campo `configJson` para generación dinámica de configuración
- **`Workflow`**: Cadena de ejecución. Cada paso (`WorkflowStep`) tiene acceso al histórico completo de los pasos anteriores.
- **`ContextPack`**: Bundles lógicos de Prompts + Skills + MCP Servers para dominios específicos (ej. DevOps, Security).

### 2. Capas de Aplicación
- **`Controller`**: REST APIs documentadas con Swagger.
  - **NEW**: `GET /api/mcp-servers/{id}/config-template` - Genera config con placeholders de env vars
- **`Service`**: Lógica de negocio pesada. *Importante:* Buscar siempre los "Engines" para lógica de ejecución (`WorkflowExecutionEngine`, `MCPTesterService`).
  - **NEW**: `MCPServerService.generateConfigJson()` y `getConfigTemplate()`
  - **NEW**: `StatsService.getGlobalStats()` incluye `totalWorkflows` y `totalContextPacks`
- **`GroqClient`**: Único cliente para inferencia de IA. Usa `llama-3.3-70b-versatile` por defecto. **No introducir otros clientes sin permiso.**
- **`DTO`**: Estricta separación entre modelos JPA y objetos de transferencia de datos.
  - **NEW**: `GlobalStatsDTO` con campos `totalWorkflows` y `totalContextPacks`
  - **NEW**: `MCPServerDTO` con campo `configJson`

---

## 🛠️ Reglas de Oro para Desarrollo

1.  **Contexto Acumulativo**: Al modificar el `WorkflowExecutionEngine`, asegúrate de que el historial de entradas/salidas se mantenga íntegro entre pasos.
2.  **Validación MCP**: Cualquier cambio en `MCPServer` debe pasar por `MCPValidatorService` (estructura JSON) y `MCPConnectivityService` (HTTP ping).
3.  **Generación de Skills**: El `SkillBuilderService` utiliza IA para "predecir" parámetros. Respeta el formato de respuesta del DTO `SkillBuildResult`.
4.  **Base de Datos**: H2 en desarrollo, PostgreSQL en producción. Mantén las migraciones/data.sql actualizadas para el seeding.
5.  **Estilo de Código**: Java 17 idiomático, anotaciones de Lombok obligatorias, JPA nativo para consultas complejas en `Repository`.
6.  **Jackson Configuration**: `spring.jackson.default-property-inclusion: always` para incluir todos los campos en JSON.

---

## 🚦 Comandos Críticos
- **Build**: `mvn clean install` (desde `backend/`)
- **Run**: `mvn spring-boot:run`
- **Tests**: `mvn test`
- **Swagger**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console` (user: `sa`, no password)
- **Frontend Dev**: `npm run dev` (desde `ai_studio/`)
- **Frontend**: `http://localhost:3000`

---

## 📊 Seed Data (Pre-loaded)

La aplicación se inicia con datos de ejemplo:
- **5 Categories**: code-generation, debugging, refactoring, testing, documentation
- **3 Prompts**: Java Bug Fixer, React Component Generator, Unit Test Generator
- **6 MCP Servers**: GitHub, Filesystem, PostgreSQL, Slack, Brave Search, Memory
- **3 Skills**: Code Reviewer Expert, Test Generator Pro, API Documentation Writer
- **3 Workflows**: Pre-configured automation pipelines
- **4 Context Packs**: AI Development, Security Audit, Database Development, Content Writing

---

## 🔌 Endpoints Importantes

### Statistics
```bash
GET /api/stats/global
```
Response:
```json
{
  "totalPrompts": 3,
  "totalImprovements": 0,
  "totalMcpServers": 6,
  "totalSkills": 3,
  "totalWorkflows": 3,
  "totalContextPacks": 4,
  "improvementRatio": 0.0
}
```

### MCP Configuration Template
```bash
GET /api/mcp-servers/{id}/config-template
```
Response:
```json
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "<GITHUB_TOKEN_VALUE>"
      }
    }
  }
}
```

---

## 🤖 Directrices de Interacción con Gemini
- **Si pides cambios en IA**: Solo usa `GroqClient`.
- **Si creas una entidad**: Asegúrate de añadir su DTO, Repository, Service, y Controller correspondientes.
- **Si corriges un bug**: Verifica primero el historial de `docs/issues/` para no reintroducir errores de migraciones previas.
- **Si trabajas con MCP**: Usa el endpoint `config-template` para mostrar variables de entorno en el frontend.
- **Si necesitas estadísticas**: Usa `StatsService.getGlobalStats()` que incluye todos los contadores.

---

## 📁 Documentación Adicional
- [README.md](./README.md) - Guía principal del proyecto
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Arquitectura técnica detallada
- [AGENTS.md](./AGENTS.md) - Guía para construir agentes de IA
- [docs/PROJECT_STATUS.md](./docs/PROJECT_STATUS.md) - Estado actual y backlog
- [docs/FRONTEND_MCP_CONFIG_FIX.md](./docs/FRONTEND_MCP_CONFIG_FIX.md) - Fix para MCP Config
