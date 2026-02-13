### ISSUE 02: application.yml

**Comando**: `Implementa ISSUE 02`

**Archivo**: `backend/src/main/resources/application.yml`

**Contenido**:
```yaml
spring:
  application:
    name: promptvault
  profiles:
    active: dev
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha

server:
  port: 8080
  error:
    include-message: always
    include-binding-errors: always
```

**Verificar**: Archivo creado en `backend/src/main/resources/`
