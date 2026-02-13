### ISSUE 12: data.sql (Seed Data)

**Comando**: `Implementa ISSUE 12`

**Archivo**: `backend/src/main/resources/data.sql`

**Contenido**:
```sql
-- Categorías iniciales
INSERT INTO categories (name, description, icon, color) VALUES
('code-generation', 'Prompts para generar código', 'code', '#4CAF50'),
('debugging', 'Prompts para encontrar y corregir bugs', 'bug', '#F44336'),
('refactoring', 'Prompts para mejorar código existente', 'refresh', '#2196F3'),
('testing', 'Prompts para generar tests unitarios', 'check', '#FF9800'),
('documentation', 'Prompts para generar documentación', 'book', '#9C27B0')
ON CONFLICT (name) DO NOTHING;

-- Prompts de ejemplo
INSERT INTO prompts (title, description, content, category, tags, is_favorite, usage_count, status) VALUES
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
4. Sugiere cómo prevenir bugs similares', 'debugging', ARRAY['java', 'spring-boot', 'bug-fix'], true, 0, 'published'),

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
- JSDoc comments', 'code-generation', ARRAY['react', 'typescript', 'tailwind'], true, 0, 'published'),

('Unit Test Generator', 'Genera tests unitarios completos', 'Genera tests unitarios para esta función:

CÓDIGO:
{pegar función}

FRAMEWORK: {JUnit 5 / Jest / pytest}

GENERA:
- Tests para happy path
- Tests para casos edge
- Tests para manejo de errores
- Mocks apropiados
- Assertions claras', 'testing', ARRAY['testing', 'tdd', 'unit-tests'], false, 0, 'published');
```

**Verificar**: 
```bash
mvn spring-boot:run
# Abrir H2 Console: http://localhost:8080/h2-console
# Ver tablas: SELECT * FROM categories; (debe haber 5 filas)
# Ver prompts: SELECT * FROM prompts; (debe haber 3 filas)
```