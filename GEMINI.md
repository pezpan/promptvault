# 🧠 Gemini CLI Context: PromptVault

## 📌 Proyecto: PromptVault
**PromptVault** es una plataforma de orquestación de agentes de IA de próxima generación. Gestiona prompts, construye "Skills" parametrizadas y orquesta "Workflows" complejos con contexto acumulativo, utilizando el protocolo MCP para la integración de herramientas externas.

---

## 🏗️ Arquitectura Técnica (Spring Boot 3 + Java 17)

### 1. Entidades de Dominio (Corazón del Sistema)
- **`Prompt`**: Unidad básica. Soporta versiones y mejoras sugeridas por IA.
- **`Skill`**: Plantillas parametrizadas (`{{param}}`). El `SkillBuilderService` genera estas plantillas automáticamente.
- **`MCPServer`**: Configuración para herramientas externas (Model Context Protocol). Soporta validación estática y de conectividad.
- **`Workflow`**: Cadena de ejecución. Cada paso (`WorkflowStep`) tiene acceso al histórico completo de los pasos anteriores.
- **`ContextPack`**: Bundles lógicos de Prompts + Skills + MCP Servers para dominios específicos (ej. DevOps, Security).

### 2. Capas de Aplicación
- **`Controller`**: REST APIs documentadas con Swagger.
- **`Service`**: Lógica de negocio pesada. *Importante:* Buscar siempre los "Engines" para lógica de ejecución (`WorkflowExecutionEngine`, `MCPTesterService`).
- **`GroqClient`**: Único cliente para inferencia de IA. Usa `llama-3.3-70b-versatile` por defecto. **No introducir otros clientes sin permiso.**
- **`DTO`**: Estricta separación entre modelos JPA y objetos de transferencia de datos.

---

## 🛠️ Reglas de Oro para Desarrollo

1.  **Contexto Acumulativo**: Al modificar el `WorkflowExecutionEngine`, asegúrate de que el historial de entradas/salidas se mantenga íntegro entre pasos.
2.  **Validación MCP**: Cualquier cambio en `MCPServer` debe pasar por `MCPValidatorService` (estructura JSON) y `MCPConnectivityService` (HTTP ping).
3.  **Generación de Skills**: El `SkillBuilderService` utiliza IA para "predecir" parámetros. Respeta el formato de respuesta del DTO `SkillBuildResult`.
4.  **Base de Datos**: H2 en desarrollo, PostgreSQL en producción. Mantén las migraciones/data.sql actualizadas para el seeding.
5.  **Estilo de Código**: Java 17 idiomático, anotaciones de Lombok obligatorias, JPA nativo para consultas complejas en `Repository`.

---

## 🚦 Comandos Críticos
- **Build**: `mvn clean install` (desde `backend/`)
- **Run**: `mvn spring-boot:run`
- **Tests**: `mvn test`
- **Swagger**: `http://localhost:8080/swagger-ui.html`

---

## 🤖 Directrices de Interacción con Gemini
- **Si pides cambios en IA**: Solo usa `GroqClient`.
- **Si creas una entidad**: Asegúrate de añadir su DTO, Repository, Service, y Controller correspondientes.
- **Si corriges un bug**: Verifica primero el historial de `docs/issues/` para no reintroducir errores de migraciones previas.
