# 🗺️ PromptVault Project Map

Este documento detalla la distribución de responsabilidades en el repositorio para facilitar la navegación técnica.

---

## 📂 Backend (Spring Boot 3)

### 📦 `com.promptvault.model` (Entidades JPA)
- **`Prompt.java`**: Gestión de texto, versiones y tags.
- **`Skill.java`**: Plantillas con placeholders.
- **`MCPServer.java`**: Configuración de servidores de herramientas.
- **`Workflow.java`**: Definición de flujos.
- **`WorkflowStep.java`**: Paso individual de un flujo.
- **`ContextPack.java`**: Bundles de recursos.

### 📦 `com.promptvault.service` (Motores y Lógica)
- **`WorkflowExecutionEngine.java`**: Lógica de ejecución secuencial con contexto acumulativo. **(CRÍTICO)**
- **`GroqClient.java`**: Integración con la API de Groq para inferencia.
- **`SkillBuilderService.java`**: Generación automática de skills mediante IA.
- **`MCPTesterService.java`**: Orquestación de pruebas para servidores MCP.
- **`MCPValidatorService.java`**: Validación estática de estructuras JSON.
- **`MCPConnectivityService.java`**: Validación dinámica de endpoints.
- **`StatsService.java`**: Agregación de métricas de uso del sistema.

### 📦 `com.promptvault.config` (Configuraciones)
- **`WorkflowSeedData.java`**: Seeding de flujos de ejemplo al iniciar.
- **`OpenAPIConfig.java`**: Configuración de Swagger/OpenAPI.
- **`CorsConfig.java`**: Políticas de seguridad de origen.

### 📦 `com.promptvault.dto` (Contratos de API)
- **`WorkflowExecuteRequest.java` / `WorkflowExecutionResult.java`**: Entrada/Salida de flujos.
- **`SkillBuildRequest.java` / `SkillBuildResult.java`**: Contratos para la generación de skills.
- **`MCPTestResult.java`**: Resultados detallados de validación MCP.

---

## 📂 Documentación & Recursos
- **`docs/issues/`**: Histórico detallado de la evolución del proyecto (01 al 57). Útil para entender decisiones arquitectónicas pasadas.
- **`backend/src/main/resources/data.sql`**: Script de inicialización de base de datos para desarrollo.
- **`backend/src/main/resources/application.yml`**: Configuración principal (perfiles dev/prod).

---

## 🚀 Flujo de Ejecución de un Workflow (Resumen)
1. **Controller**: Recibe `WorkflowExecuteRequest`.
2. **Service**: Llama al `WorkflowExecutionEngine`.
3. **Engine**: 
   - Itera sobre los `WorkflowStep`.
   - Recupera la `Skill` o `Prompt` de cada paso.
   - Envía el contexto acumulado al `GroqClient`.
   - Almacena la salida para el siguiente paso.
4. **Response**: Devuelve `WorkflowExecutionResult` con la traza completa.
