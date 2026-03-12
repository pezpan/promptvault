# 🤖 Building Agents with PromptVault

PromptVault provides the foundational infrastructure to build, test, and deploy sophisticated AI agents. This guide explains how to use our systems to move from static prompts to autonomous agents.

## 🔗 Chained Intelligence: Workflows

A **Workflow** in PromptVault is more than a list of steps; it's a **chain of thought**. 

### Cumulative Context
Unlike traditional pipelines, each step in a PromptVault Workflow has access to the full history of the interaction. 
- **Step 1** analyzes code.
- **Step 2** generates tests based on **Step 1's** analysis.
- **Step 3** summarizes everything for a human reviewer.

This allows the AI to maintain coherence and follow complex instructions that a single prompt couldn't handle.

### Workflow Step Types
Each step in a workflow must be one of three types:

1. **SKILL** — Uses a parameterized template from the Skill library
   - Requires `skillId` pointing to an existing Skill
   - Supports `skillParameters` map to fill placeholders (`{{PARAM_NAME}}`)
   - Special placeholder `__PREVIOUS_OUTPUT__` automatically inserts the previous step's result
   - Located in: `WorkflowController#create()` → `WorkflowService` → `WorkflowExecutionEngine`

2. **FREE_PROMPT** — Executes arbitrary prompt text
   - Use `{{PREVIOUS_OUTPUT}}` to reference the previous step's output
   - Ideal for custom logic between Skill calls
   - Example: Summarization, filtering, or conditional logic

3. **TRANSFORM** — Built-in text transformations (no parameters needed)
   - Valid types: `SUMMARIZE`, `TRANSLATE_ES`, `TRANSLATE_EN`, `FORMAT_MARKDOWN`, `EXTRACT_KEYWORDS`
   - Implemented in `WorkflowExecutionEngine.buildAccumulativePrompt()`
   - No configuration needed, just specify the `transformType`

### How to use:
Execute a workflow via `POST /api/workflows/{id}/execute`. Just provide the `initialInput` and PromptVault handles the rest. The engine maintains cumulative context via `accumulatedContext` List in `WorkflowExecutionEngine` (line ~50).

---

## 🛠️ Tool Use: Model Context Protocol (MCP)

Agents need tools. PromptVault uses the **Model Context Protocol** to bridge the gap between LLMs and your local/remote data.

### Supported Servers
- **Standard I/O (Local)**: Run tools like `npx`, `uvx`, or local Python scripts to access your filesystem or run code.
- **HTTP/SSE (Remote)**: Connect to self-hosted or managed services.

### Validation
Don't guess if your agent's tools are working. Use the **MCP Tester** (`POST /api/mcp-servers/{id}/test`) to verify:
1. **Static Validation**: Is the JSON configuration correct?
   - Handled by `MCPValidatorService.validate()` — checks schema compliance
2. **Connectivity**: Is the remote server reachable?
   - Handled by `MCPConnectivityService.testConnectivity()` — performs HTTP ping

### Key MCP Services
- **`MCPServerService`**: CRUD operations for MCP server configurations
- **`MCPValidatorService`**: Static JSON schema validation (checks required fields, types)
- **`MCPConnectivityService`**: Real-time HTTP connectivity testing
- **`MCPTesterService`**: Orchestration layer that runs both validators (see `testById()` method)

---

## 🏗️ Meta-AI: Skill Builder

The **Skill Builder** is our "AI generating AI" feature. If you know what you want your agent to do but don't know how to write the perfect prompt, the Skill Builder will do it for you.

**Process**:
1. Describe your goal (e.g., "I want a tool to audit AWS IAM policies").
2. Provide example inputs.
3. The AI generates a parameterized **Skill** with a professional template, detected parameters, and quality scoring.

**Implementation**: 
- Call `SkillBuilderService.buildSkill(SkillBuildRequest)` to generate skills via `GroqClient`
- Uses `SkillBuildResult` and `GeneratedSkill` DTOs to structure the response
- Automatically detects parameters and creates a reusable skill in the database
- Access via `POST /api/skills/build` endpoint in `SkillController`

---

## 📦 Rapid Deployment: Context Packs

**Context Packs** are our "App Store" for AI agents. They bundle together:
- The right **Prompts** for the task.
- The necessary **Skills** (parameterized templates).
- The required **MCP Servers** (tools).

Instead of configuring 10 different things, you load one Pack and your agent is ready to work in a specific domain (e.g., `Security Audit Pack`).

---

## 🚀 Development Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- [Groq API Key](https://console.groq.com/) (free tier available: 30 req/min, 14,400/day)

### Configuration
Set your Groq API key as an environment variable:
```bash
# macOS/Linux
export GROQ_API_KEY=your_key_here

# Windows PowerShell
$env:GROQ_API_KEY="your_key_here"
```

### Running Locally
```bash
cd backend
mvn spring-boot:run
```
Server starts at `http://localhost:8080`

### Database
- **Development**: H2 in-memory database (auto-initialized)
  - H2 Console: `http://localhost:8080/h2-console` (username: `sa`, no password)
- **Production**: PostgreSQL (configured via environment variables)

### API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

### Build & Test
```bash
# Compile and run tests
mvn clean install

# Run tests only
mvn test

# Build JAR for deployment
mvn clean package
```

---

## 🏛️ Architecture Overview

### Service Layer
Core services in `com.promptvault.service`:
- **`WorkflowExecutionEngine`**: Orchestrates multi-step workflows with cumulative context (see `execute()` method)
- **`GroqClient`**: Single point for AI API calls to Groq (model: `llama-3.3-70b-versatile`)
- **`SkillBuilderService`**: Auto-generates Skills from natural language descriptions
- **`MCPTesterService`**: Validates and tests MCP server configurations
- **`PromptService`, `SkillService`, `ContextPackService`**: CRUD operations for core entities
- **`StatsService`**: Aggregates usage metrics (execution counts, timestamps)

### Code Organization
- **`model/`**: JPA Entities (`Workflow`, `Skill`, `MCPServer`, `ContextPack`, etc.)
- **`dto/`**: Request/Response contracts (strict separation from JPA entities)
- **`repository/`**: Spring Data JPA interfaces with optimized queries
- **`controller/`**: REST endpoints with Swagger annotations
- **`config/`**: Seed data, CORS, OpenAPI setup

### Key Patterns
1. **Lombok Usage**: `@Data`, `@RequiredArgsConstructor`, `@Slf4j` are mandatory for new classes
2. **DTOs**: Always use DTOs in REST responses; never expose JPA entities directly
3. **Repository Queries**: Use `@Query` for complex JPQL; keep business logic in Services
4. **Error Handling**: Extend `RuntimeException`; custom exceptions in `com.promptvault.exception`
5. **Logging**: Use SLF4J via Lombok's `@Slf4j` — log at appropriate levels (DEBUG for details, INFO for flow)

## 🚀 Advanced Agentic Patterns

### 1. The Reviewer-Fixer Pattern
Create a 2-step Workflow:
- **Step 1 (SKILL: Bug Hunter)**: Identifies issues.
- **Step 2 (FREE_PROMPT)**: "Based on the previous analysis, provide the code fix."

### 2. The Multilingual Documentation Agent
- **Step 1 (SKILL: API Documentation Writer)**: Generates English docs.
- **Step 2 (TRANSFORM: TRANSLATE_ES)**: Translates to Spanish.
- **Step 3 (TRANSFORM: FORMAT_MARKDOWN)**: Ensures perfect formatting.

### 3. Custom Workflows with Context Packs
Bundle related Skills + MCP Servers + Prompts into a ContextPack:
1. Create Skills via `POST /api/skills/build` (or upload manually)
2. Register MCP Servers: `POST /api/mcp-servers` (validate with `POST /api/mcp-servers/{id}/test`)
3. Create Workflow: `POST /api/workflows` (compose SKILL, FREE_PROMPT, TRANSFORM steps)
4. Bundle into ContextPack: `POST /api/context-packs` (groups related resources)
5. Execute: `POST /api/workflows/{id}/execute` with initial input

---

## 📚 Extending PromptVault

### Adding a New TRANSFORM Type
1. Add enum value to `WorkflowStep.TransformType`
2. Implement handler in `WorkflowExecutionEngine.buildAccumulativePrompt()` (look for `switch(transformType)`)
3. Test via workflow execution with `POST /api/workflows/{id}/execute`

### Adding a New MCP Server
1. Create server configuration in database (or seed in `data.sql`)
2. Test validation: `POST /api/mcp-servers/{id}/test?testLevel=STATIC` (JSON schema)
3. Test connectivity: `POST /api/mcp-servers/{id}/test?testLevel=CONNECTIVITY` (HTTP ping)
4. Reference in Workflows or ContextPacks

### Creating Custom Skills
- **Manual**: `POST /api/skills` with template and parameters
- **AI-Generated**: `POST /api/skills/build` with objective and examples
- Both accept `{{PARAM_NAME}}` placeholders for parameter substitution

---

*Ready to build? Start by exploring the [predefined workflows](http://localhost:8080/swagger-ui.html#/Workflows).*
