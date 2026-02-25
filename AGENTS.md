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

### How to use:
Execute a workflow via `POST /api/workflows/{id}/execute`. Just provide the `initialInput` and PromptVault handles the rest.

---

## 🛠️ Tool Use: Model Context Protocol (MCP)

Agents need tools. PromptVault uses the **Model Context Protocol** to bridge the gap between LLMs and your local/remote data.

### Supported Servers
- **Standard I/O (Local)**: Run tools like `npx`, `uvx`, or local Python scripts to access your filesystem or run code.
- **HTTP/SSE (Remote)**: Connect to self-hosted or managed services.

### Validation
Don't guess if your agent's tools are working. Use the **MCP Tester** (`POST /api/mcp-servers/{id}/test`) to verify:
1. **Static Validation**: Is the JSON configuration correct?
2. **Connectivity**: Is the remote server reachable?

---

## 🏗️ Meta-AI: Skill Builder

The **Skill Builder** is our "AI generating AI" feature. If you know what you want your agent to do but don't know how to write the perfect prompt, the Skill Builder will do it for you.

**Process**:
1. Describe your goal (e.g., "I want a tool to audit AWS IAM policies").
2. Provide example inputs.
3. The IA generates a parameterized **Skill** with a professional template, detected parameters, and quality scoring.

---

## 📦 Rapid Deployment: Context Packs

**Context Packs** are our "App Store" for AI agents. They bundle together:
- The right **Prompts** for the task.
- The necessary **Skills** (parameterized templates).
- The required **MCP Servers** (tools).

Instead of configuring 10 different things, you load one Pack and your agent is ready to work in a specific domain (e.g., `Security Audit Pack`).

---

## 🚀 Advanced Agentic Patterns

### 1. The Reviewer-Fixer Pattern
Create a 2-step Workflow:
- **Step 1 (SKILL: Bug Hunter)**: Identifies issues.
- **Step 2 (FREE_PROMPT)**: "Based on the previous analysis, provide the code fix."

### 2. The Multilingual Documentation Agent
- **Step 1 (SKILL: API Documentation Writer)**: Generates English docs.
- **Step 2 (TRANSFORM: TRANSLATE_ES)**: Translates to Spanish.
- **Step 3 (TRANSFORM: FORMAT_MARKDOWN)**: Ensures perfect formatting.

---
*Ready to build? Start by exploring the [predefined workflows](http://localhost:8080/swagger-ui.html#/Workflows).*
