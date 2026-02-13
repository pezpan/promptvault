# PromptVault - Technical Architecture

## 📐 System Architecture

### Overview

PromptVault follows a modern three-tier architecture:

1. **Presentation Layer**: React SPA with Vite
2. **Business Logic Layer**: Spring Boot REST API
3. **Data Layer**: PostgreSQL relational database

### Architecture Diagram

```
┌────────────────────────────────────────────────────────────┐
│                    Client Layer                             │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  React Application (Vite)                                  │
│  ├── Pages (Dashboard, Library, Editor)                   │
│  ├── Components (PromptCard, Editor, Filters)             │
│  ├── Services (api.js, gemini.js)                         │
│  ├── State Management (Context API / Zustand)             │
│  └── Routing (React Router)                               │
│                                                             │
└────────────────────┬───────────────────────────────────────┘
                     │ HTTP/REST (JSON)
┌────────────────────▼───────────────────────────────────────┐
│                  API Layer (Spring Boot)                    │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  Controllers (REST Endpoints)                              │
│  ├── PromptController.java                                │
│  ├── CategoryController.java                              │
│  ├── SearchController.java                                │
│  └── AIController.java                                    │
│                                                             │
│  Services (Business Logic)                                 │
│  ├── PromptService.java                                   │
│  ├── CategoryService.java                                 │
│  ├── SearchService.java                                   │
│  ├── AIEnhancementService.java                            │
│  └── ExportService.java                                   │
│                                                             │
│  Repositories (Data Access)                                │
│  ├── PromptRepository.java (JPA)                          │
│  ├── CategoryRepository.java (JPA)                        │
│  └── ImprovementHistoryRepository.java (JPA)              │
│                                                             │
│  Models (Domain Objects)                                   │
│  ├── Prompt.java (@Entity)                                │
│  ├── Category.java (@Entity)                              │
│  └── PromptImprovement.java (@Entity)                     │
│                                                             │
│  DTOs (Data Transfer Objects)                              │
│  ├── PromptDTO.java                                       │
│  ├── PromptCreateRequest.java                             │
│  ├── PromptUpdateRequest.java                             │
│  ├── AIImprovementResponse.java                           │
│  └── SearchRequest.java                                   │
│                                                             │
└────────┬────────────────────────┬──────────────────────────┘
         │                        │
         │                        │ External API Call
         │                        │
┌────────▼────────┐      ┌───────▼──────────┐
│   PostgreSQL    │      │   Gemini API     │
│                 │      │  (Google AI)     │
│  Tables:        │      │                  │
│  - prompts      │      │  Endpoints:      │
│  - categories   │      │  - generateContent│
│  - improvements │      │  - embedContent  │
└─────────────────┘      └──────────────────┘
```

---

## 🗂️ Project Structure

### Backend (Spring Boot)

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/promptvault/
│   │   │   ├── PromptVaultApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── OpenAPIConfig.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── PromptController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── SearchController.java
│   │   │   │   └── AIController.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── PromptService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── SearchService.java
│   │   │   │   ├── AIEnhancementService.java
│   │   │   │   └── ExportService.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── PromptRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   └── ImprovementHistoryRepository.java
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Prompt.java
│   │   │   │   ├── Category.java
│   │   │   │   └── PromptImprovement.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── PromptDTO.java
│   │   │   │   ├── PromptCreateRequest.java
│   │   │   │   ├── PromptUpdateRequest.java
│   │   │   │   ├── AIImprovementResponse.java
│   │   │   │   └── SearchRequest.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── AIServiceException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   └── util/
│   │   │       ├── PromptAnalyzer.java
│   │   │       └── FileExporter.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── data.sql (seed data)
│   │
│   └── test/
│       └── java/com/promptvault/
│           ├── controller/
│           ├── service/
│           └── repository/
│
├── pom.xml
└── Dockerfile
```

### Frontend (React + Vite)

```
frontend/
├── public/
│   └── favicon.ico
│
├── src/
│   ├── main.jsx
│   ├── App.jsx
│   │
│   ├── pages/
│   │   ├── Dashboard.jsx
│   │   ├── PromptLibrary.jsx
│   │   ├── PromptEditor.jsx
│   │   └── PromptDetail.jsx
│   │
│   ├── components/
│   │   ├── common/
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Card.jsx
│   │   │   └── Modal.jsx
│   │   │
│   │   ├── prompts/
│   │   │   ├── PromptCard.jsx
│   │   │   ├── PromptList.jsx
│   │   │   ├── PromptFilters.jsx
│   │   │   └── PromptSearch.jsx
│   │   │
│   │   ├── editor/
│   │   │   ├── MarkdownEditor.jsx
│   │   │   ├── PromptPreview.jsx
│   │   │   └── AIImprover.jsx
│   │   │
│   │   └── layout/
│   │       ├── Header.jsx
│   │       ├── Sidebar.jsx
│   │       └── Footer.jsx
│   │
│   ├── services/
│   │   ├── api.js (Axios instance)
│   │   ├── promptService.js
│   │   ├── categoryService.js
│   │   └── aiService.js
│   │
│   ├── hooks/
│   │   ├── usePrompts.js
│   │   ├── useCategories.js
│   │   └── useDebounce.js
│   │
│   ├── context/
│   │   └── AppContext.jsx
│   │
│   ├── utils/
│   │   ├── formatters.js
│   │   └── validators.js
│   │
│   └── styles/
│       └── index.css (Tailwind)
│
├── package.json
├── vite.config.js
├── tailwind.config.js
└── Dockerfile
```

---

## 🔌 API Contracts

### Prompt Endpoints

#### GET /api/prompts
```json
Request:
  Query params: ?category=code&tags=java&search=bug&page=0&size=20

Response: 200 OK
{
  "content": [
    {
      "id": 1,
      "title": "Java Bug Fixer",
      "description": "Analyzes and fixes Java bugs",
      "content": "Analyze this Java code and fix the bug...",
      "category": "debugging",
      "tags": ["java", "spring", "debug"],
      "isFavorite": true,
      "usageCount": 24,
      "rating": 4.5,
      "createdAt": "2024-02-08T10:00:00Z",
      "updatedAt": "2024-02-08T15:30:00Z"
    }
  ],
  "totalElements": 45,
  "totalPages": 3,
  "number": 0,
  "size": 20
}
```

#### POST /api/prompts
```json
Request:
{
  "title": "React Component Generator",
  "description": "Generates React functional components",
  "content": "Create a React functional component...",
  "category": "code-generation",
  "tags": ["react", "javascript", "component"],
  "project": "my-app"
}

Response: 201 Created
{
  "id": 46,
  "title": "React Component Generator",
  "description": "Generates React functional components",
  "content": "Create a React functional component...",
  "category": "code-generation",
  "tags": ["react", "javascript", "component"],
  "project": "my-app",
  "isFavorite": false,
  "usageCount": 0,
  "rating": null,
  "status": "draft",
  "createdAt": "2024-02-08T16:00:00Z",
  "updatedAt": "2024-02-08T16:00:00Z",
  "version": 1
}
```

#### POST /api/prompts/{id}/improve
```json
Request:
{
  "currentContent": "Fix bug in code",
  "context": {
    "framework": "spring-boot",
    "language": "java"
  }
}

Response: 200 OK
{
  "originalContent": "Fix bug in code",
  "improvedContent": "Analyze and fix the bug in this Java Spring Boot code:\n\nCONTEXT:\n- Framework: Spring Boot 3.x\n- Language: Java 17\n\nSTEPS:\n1. Identify the root cause\n2. Propose a fix with explanation\n3. Show before/after comparison\n4. Suggest prevention pattern\n\nCODE:\n{paste code here}",
  "improvements": [
    "Added clear structure with CONTEXT section",
    "Specified framework and language versions",
    "Broke down into actionable steps",
    "Added placeholder for code",
    "More specific and actionable"
  ],
  "tokenUsed": {
    "input": 45,
    "output": 180
  }
}
```

---

## 🔐 Security Considerations

### v1.0 (Public Access)
- No authentication required
- All prompts are public
- CORS enabled for frontend domain only
- Rate limiting on AI endpoints (10 requests/min per IP)

### v2.0 (With Auth)
- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Private/Public prompts
- API key management for external access

---

## 🗄️ Database Design

### Entity Relationships

```
┌─────────────┐         ┌──────────────────┐
│  Category   │◄────────│     Prompt       │
│             │  1:N    │                  │
│ - id        │         │ - id             │
│ - name      │         │ - title          │
│ - icon      │         │ - content        │
└─────────────┘         │ - category_id    │
                        │ - tags[]         │
                        │ - is_favorite    │
                        └────────┬─────────┘
                                 │ 1:N
                        ┌────────▼──────────────┐
                        │ PromptImprovement     │
                        │                       │
                        │ - id                  │
                        │ - prompt_id           │
                        │ - original_content    │
                        │ - improved_content    │
                        │ - accepted            │
                        └───────────────────────┘
```

### Indexes

```sql
-- Performance optimization indexes
CREATE INDEX idx_prompts_category ON prompts(category);
CREATE INDEX idx_prompts_created_at ON prompts(created_at DESC);
CREATE INDEX idx_prompts_tags ON prompts USING GIN(tags);
CREATE INDEX idx_prompts_favorite ON prompts(is_favorite) WHERE is_favorite = true;
CREATE INDEX idx_prompts_search ON prompts USING GIN(to_tsvector('english', title || ' ' || description || ' ' || content));
```

---

## 🧩 Key Components

### AIEnhancementService

```java
@Service
public class AIEnhancementService {
    private final GeminiClient geminiClient;
    
    public AIImprovementResponse improvePrompt(String originalPrompt, Map<String, String> context) {
        // 1. Analyze current prompt structure
        PromptAnalysis analysis = analyzePrompt(originalPrompt);
        
        // 2. Build improvement prompt for Gemini
        String improvementPrompt = buildImprovementPrompt(originalPrompt, context, analysis);
        
        // 3. Call Gemini API
        GeminiResponse response = geminiClient.generateContent(improvementPrompt);
        
        // 4. Parse and structure response
        return parseImprovementResponse(response, originalPrompt);
    }
    
    private String buildImprovementPrompt(String original, Map<String, String> context, PromptAnalysis analysis) {
        return String.format("""
            You are an expert in prompt engineering.
            
            Current prompt:
            %s
            
            Context:
            - Framework: %s
            - Language: %s
            
            Analysis:
            - Has clear goal: %s
            - Has context section: %s
            - Has constraints: %s
            - Has examples: %s
            
            Improve this prompt by:
            1. Adding clear structure (GOAL, CONTEXT, REQUIREMENTS, OUTPUT FORMAT)
            2. Making it more specific and actionable
            3. Adding relevant constraints
            4. Suggesting format for code/examples
            
            Return ONLY the improved prompt, then on a new line list the key improvements made.
            """, original, context.get("framework"), context.get("language"), 
            analysis.hasGoal(), analysis.hasContext(), analysis.hasConstraints(), analysis.hasExamples());
    }
}
```

---

## 📊 Performance Considerations

### Caching Strategy
- Categories: Redis cache (rarely change)
- Popular prompts: In-memory cache with 1-hour TTL
- Search results: Short-term cache (5 minutes)

### Database Optimization
- Pagination for all list endpoints (default 20 items)
- Lazy loading of prompt improvements
- Database connection pooling (HikariCP)

### API Rate Limiting
- Gemini API: Max 10 calls/minute per user
- Search API: Max 30 calls/minute per user
- Export API: Max 5 calls/minute per user

---

## 🔄 CI/CD Pipeline

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD

on: [push, pull_request]

jobs:
  backend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: cd backend && mvn test
      
  frontend-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - run: cd frontend && npm test
      
  deploy:
    needs: [backend-test, frontend-test]
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to production
        run: echo "Deploy step"
```

---

## 🧪 Testing Strategy

### Backend Tests
- **Unit Tests**: Services (business logic)
- **Integration Tests**: Controllers (API endpoints)
- **Repository Tests**: JPA queries

### Frontend Tests
- **Component Tests**: React Testing Library
- **Integration Tests**: User flows
- **E2E Tests**: Playwright (future)

---

## 📈 Monitoring & Observability

### Metrics to Track
- API response times
- Gemini API call success rate
- Database query performance
- User engagement (prompts created, improved)

### Logging
- Structured logging with Logback
- Log levels: ERROR, WARN, INFO, DEBUG
- Correlation IDs for request tracking

---

Last updated: 2024-02-08
