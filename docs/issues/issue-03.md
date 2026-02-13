### ISSUE 03: application-dev.yml

**Comando**: `Implementa ISSUE 03`

**Archivo**: `backend/src/main/resources/application-dev.yml`

**Contenido**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:promptvaultdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

logging:
  level:
    com.promptvault: DEBUG
```

**Verificar**: Archivo creado
