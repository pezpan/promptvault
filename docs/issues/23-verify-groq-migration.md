# ISSUE 23: Verificación Final - Pruebas con Groq

Verifica que la migración a Groq está completa y funcional.

## Pre-requisitos

Antes de ejecutar este issue, asegúrate de:

✅ Issue 21 completado (GroqClient creado)
✅ Issue 22 completado (AIEnhancementService actualizado)
✅ API Key de Groq obtenida y configurada en `application-dev.yml`

## Pasos de Verificación

### 1. Verificar que NO existen referencias a Gemini

**Comando**:
```bash
cd backend/src/main/java/com/promptvault

# Buscar referencias a GeminiClient (no debe encontrar nada):
grep -r "GeminiClient" .

# Buscar referencias a "gemini" en código (puede aparecer en comentarios):
grep -r "gemini" . --include="*.java"
```

**Resultado esperado**: No debe encontrar archivos Java que usen `GeminiClient`.

---

### 2. Verificar que GroqClient existe

**Comando**:
```bash
ls backend/src/main/java/com/promptvault/service/GroqClient.java
```

**Resultado esperado**: El archivo debe existir.

---

### 3. Verificar configuración

**Archivo**: `backend/src/main/resources/application-dev.yml`

**Verificar que contenga**:
```yaml
groq:
  api:
    key: gsk_...
```

**Acción**: Si la key no está configurada, añadirla ahora.

Obtener key en: https://console.groq.com/keys

---

### 4. Compilar el proyecto

**Comando**:
```bash
cd backend
mvn clean compile
```

**Resultado esperado**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: X s
```

Si hay errores de compilación, revisar:
- ¿GeminiClient sigue siendo referenciado en algún archivo?
- ¿GroqClient.java está creado correctamente?
- ¿AIEnhancementService usa GroqClient?

---

### 5. Ejecutar la aplicación

**Comando**:
```bash
mvn spring-boot:run
```

**Resultado esperado**:
```
Started PromptVaultApplication in X seconds
```

**Verificar en logs**: NO debe aparecer ningún error relacionado con Gemini o Groq.

---

### 6. Probar el endpoint /improve en Swagger

**Pasos**:

1. **Abrir Swagger**: http://localhost:8080/swagger-ui.html

2. **Ir a**: `POST /api/prompts/{id}/improve`

3. **Ejecutar con id = 1** (o cualquier ID de prompt existente)

4. **Esperar respuesta** (debería tardar 1-2 segundos)

**Respuesta esperada** (200 OK):
```json
{
  "originalContent": "Analiza el siguiente código Java y encuentra el bug:...",
  "improvedContent": "# 🎯 OBJETIVO\nAnalizar y corregir bugs en código Java...\n\n# 📋 CONTEXTO\n...",
  "improvements": [
    "Añadida estructura clara con secciones (OBJETIVO, CONTEXTO, REQUISITOS)",
    "Especificados los pasos a seguir de forma detallada",
    "Definido formato de salida esperado",
    "Incluido contexto técnico relevante"
  ],
  "tokenUsage": null
}
```

**Si hay error 500**:
- Revisar logs del backend
- Verificar que la API key de Groq es correcta
- Verificar que Groq API está disponible (https://status.groq.com)

---

### 7. Probar múltiples veces (verificar límites)

**Pasos**:

1. Ejecutar `POST /api/prompts/1/improve` 
2. Ejecutar `POST /api/prompts/2/improve`
3. Ejecutar `POST /api/prompts/3/improve`

**Resultado esperado**: 
- ✅ Todas las peticiones funcionan
- ✅ Respuestas en 1-3 segundos
- ✅ No hay errores de rate limit (límite: 30 req/min)

---

### 8. Verificar logs

**Revisar logs del backend**:

Buscar líneas como:
```
DEBUG c.p.service.GroqClient : Llamando a Groq API (modelo: llama-3.3-70b-versatile)...
DEBUG c.p.service.GroqClient : Respuesta de Groq recibida: 542 caracteres
INFO  c.p.service.AIEnhancementService : Mejorando prompt con ID: 1
```

**NO debe aparecer**:
```
Error llamando a Gemini API
GeminiClient
```

---

### 9. Probar otros endpoints (verificar que no se rompieron)

**Probar en Swagger**:

1. ✅ `GET /api/prompts` - Listar prompts
2. ✅ `POST /api/prompts` - Crear prompt
3. ✅ `GET /api/prompts/1` - Ver prompt
4. ✅ `PUT /api/prompts/1` - Actualizar
5. ✅ `DELETE /api/prompts/1` - Eliminar
6. ✅ `GET /api/prompts/1/export?format=txt` - Exportar
7. ✅ `GET /api/categories` - Listar categorías

**Resultado esperado**: Todos funcionan correctamente.

---

### 10. Probar mejora de prompts largos

**Crear un prompt con mucho texto**:

```json
POST /api/prompts
{
  "title": "Prompt Largo de Prueba",
  "description": "Testing Groq con contenido extenso",
  "content": "Este es un prompt muy largo con muchas instrucciones y detalles para verificar que Groq puede manejarlo correctamente sin problemas de límites de tokens...",
  "category": "testing",
  "tags": ["test", "groq"]
}
```

**Luego mejorarlo**:
```
POST /api/prompts/{nuevo_id}/improve
```

**Resultado esperado**: 
- ✅ Funciona correctamente
- ✅ Respuesta en tiempo razonable
- ✅ Mejoras coherentes

---

## Checklist Final

```
[ ] GeminiClient.java eliminado
[ ] GroqClient.java creado
[ ] AIEnhancementService actualizado
[ ] application.yml configurado con groq.api.key
[ ] API key de Groq obtenida y configurada
[ ] mvn clean compile exitoso
[ ] mvn spring-boot:run arranca sin errores
[ ] POST /api/prompts/1/improve retorna 200 OK
[ ] Respuesta contiene improvedContent e improvements
[ ] Logs muestran "Llamando a Groq API"
[ ] Todos los demás endpoints siguen funcionando
[ ] Sin errores en logs
```

---

## Resultado Final Esperado

Después de completar este issue:

✅ **Proyecto migrado completamente a Groq**
✅ **Sin referencias a Gemini en el código**
✅ **Endpoint /improve funcionando con Groq**
✅ **Respuestas rápidas (1-3 segundos)**
✅ **Sin límites de rate limit problemáticos**
✅ **Todos los servicios funcionando correctamente**

---

## Troubleshooting

### Error: "GROQ_API_KEY no está configurada"

**Solución**:
1. Verificar que `application-dev.yml` tiene:
   ```yaml
   groq:
     api:
       key: gsk_tu_key_aqui
   ```
2. Reiniciar la aplicación

### Error: 401 Unauthorized

**Solución**:
- API key incorrecta
- Generar nueva key en https://console.groq.com/keys

### Error: 429 Rate Limit

**Solución**:
- Esperar 1 minuto (límite: 30 req/min)
- Groq es mucho más generoso que Gemini, esto no debería pasar

### Respuesta muy lenta (> 10 segundos)

**Posibles causas**:
- Problema de red
- Groq API lenta temporalmente
- Verificar status: https://status.groq.com

**Solución**:
- Normalmente Groq responde en 1-2 segundos
- Si persiste, revisar logs para ver dónde se demora

---

## Comandos Rápidos de Verificación

```bash
# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run

# Probar endpoint (desde otra terminal)
curl -X POST http://localhost:8080/api/prompts/1/improve

# Ver logs en tiempo real
tail -f logs/spring-boot-logger.log
```

---

## Éxito

Si todos los checkboxes están marcados: **¡MIGRACIÓN COMPLETADA! 🎉**

Tu proyecto ahora usa Groq API:
- ✅ Gratis ilimitado (14,400 req/día)
- ✅ Ultra rápido
- ✅ Modelos potentes (Llama 3.3 70B)
- ✅ Sin problemas de rate limit

**Proyecto completo y funcional con IA integrada.**
