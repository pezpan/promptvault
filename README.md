# PromptVault

**PromptVault** is a next-generation platform for managing, building, and automating AI prompts. Beyond a simple library, it provides a robust infrastructure for AI-agent orchestration through workflows, skills, and Model Context Protocol (MCP) integration.

## 🌟 Key Features

- 📝 **Advanced Prompt Library**: CRUD operations for prompts with multi-tag support and version tracking.
- ⚡ **High-Speed AI (Groq)**: Integrated with Groq LPU™ for ultra-fast prompt enhancement and generation using Llama 3 models.
- 🔗 **Workflows**: Chain multiple prompts together with **cumulative context**. Build complex pipelines where each step learns from the previous ones.
- 🛠️ **Skills System**: Reusable, parameterized prompt templates. Includes an **AI Skill Builder** that creates tools from a simple description.
- 🔌 **MCP Integration**: Manage and validate Model Context Protocol servers to give your AI access to tools like GitHub, Slack, and PostgreSQL.
- 📦 **Context Packs**: Pre-configured bundles of Prompts, Skills, and MCP Servers for specific domains (Security, DevOps, Writing, etc.).
- 🧪 **Built-in Testers**: Static and connectivity validation for MCP configurations.

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.2.2, Spring Data JPA.
- **Database**: H2 (In-memory for dev), PostgreSQL (Production).
- **AI Engine**: Groq API (llama-3.3-70b-versatile).
- **Documentation**: Swagger/OpenAPI 3.0.

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- A [Groq API Key](https://console.groq.com/) (Free)

### Configuration
1. Clone the repository.
2. Set your Groq API Key as an environment variable:
   ```bash
   export GROQ_API_KEY=your_key_here
   ```
   *On Windows (PowerShell):* `$env:GROQ_API_KEY="your_key_here"`

### Running the Application
```bash
cd backend
mvn spring-boot:run
```
The server will start at `http://localhost:8080`.

### API Documentation
Once the app is running, you can explore the interactive API docs:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Docs**: `http://localhost:8080/api-docs`

## 📖 Architecture
For a deep dive into the technical design, entities, and flow, see [ARCHITECTURE.md](./ARCHITECTURE.md).

## 🤖 Agents & Automation
Learn how to build AI agents using our Workflow and Skill systems in [AGENTS.md](./AGENTS.md).

---
*Created with ❤️ for the AI Engineering community.*
