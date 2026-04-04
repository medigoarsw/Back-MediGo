# 🔐 Guía de Uso - API de Autenticación

## 🚀 Inicio Rápido

### 1. **Ejecutar la aplicación**

```bash
mvn spring-boot:run
```

La aplicación se iniciará en `http://localhost:8080`

### 2. **Verificar que funciona**

```bash
curl -X GET http://localhost:8080/api/auth/me?user_id=1
```

---

## 📌 Ejemplos de Uso

### 1️⃣ **LOGIN CON STUDENT**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@medigo.com",
    "password": "123"
  }'
```

**Respuesta esperada (200 OK):**
```json
{
  "access_token": "fake-jwt.1.student.1711894234567",
  "token_type": "Bearer",
  "user_id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "student",
  "expires_in": 3600
}
```

---

### 2️⃣ **LOGIN CON ADMIN**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@medigo.com",
    "password": "123"
  }'
```

**Respuesta:**
```json
{
  "access_token": "fake-jwt.2.admin.1711894234567",
  "token_type": "Bearer",
  "user_id": 2,
  "username": "admin",
  "email": "admin@medigo.com",
  "role": "admin",
  "expires_in": 3600
}
```

---

### 3️⃣ **LOGIN FALLIDO (Contraseña incorrecta)**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "student@medigo.com",
    "password": "wrongpassword"
  }'
```

**Respuesta esperada (401 Unauthorized):**
```
Empty body
```

---

### 4️⃣ **LOGIN FALLIDO (Usuario inexistente)**

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@medigo.com",
    "password": "123"
  }'
```

**Respuesta esperada (401 Unauthorized):**
```
Empty body
```

---

### 5️⃣ **OBTENER PERFIL DEL USUARIO**

```bash
curl -X GET "http://localhost:8080/api/auth/me?user_id=1" \
  -H "Accept: application/json"
```

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "student",
  "active": true
}
```

---

### 6️⃣ **OBTENER USUARIO POR ID**

```bash
curl -X GET http://localhost:8080/api/auth/1
```

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "student",
  "active": true
}
```

---

### 7️⃣ **OBTENER USUARIO POR EMAIL**

```bash
curl -X GET "http://localhost:8080/api/auth/email/admin@medigo.com"
```

**Respuesta (200 OK):**
```json
{
  "id": 2,
  "username": "admin",
  "email": "admin@medigo.com",
  "role": "admin",
  "active": true
}
```

---

## 🧪 Testing con Postman/Insomnia

### Collection JSON (Importar en Postman)

```json
{
  "info": {
    "name": "MediGo Auth API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Login Student",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"email\":\"student@medigo.com\",\"password\":\"123\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "auth", "login"]
        }
      }
    },
    {
      "name": "Get User Me",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/auth/me?user_id=1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "auth", "me"],
          "query": [
            {
              "key": "user_id",
              "value": "1"
            }
          ]
        }
      }
    }
  ]
}
```

---

## 🔍 Debug & Logs

### Ver logs detallados

En `application.properties` agrega:

```properties
logging.level.edu.escuelaing.arsw.medigo.users=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Output esperado al login:

```
[INFO ] Intentando autenticar usuario: student@medigo.com
[DEBUG] Buscando usuario por email: student@medigo.com
[INFO ] Usuario autenticado exitosamente: student@medigo.com
```

---

## 📊 Códigos de Respuesta HTTP

| Código | Significado | Ejemplo |
|--------|------------|---------|
| `200 OK` | Login o GET exitoso | `POST /api/auth/login` success |
| `400 Bad Request` | Request mal formado | Falta username/password |
| `401 Unauthorized` | Credenciales inválidas | Password incorrecto |
| `404 Not Found` | Usuario no existe | `GET /api/auth/999` |
| `500 Server Error` | Error interno | Exception no manejada |

---

## 🎓 Siguiente Paso: JWT Real

Cuando necesites JWT real, **SOLO cambias**:

1. Crear `JwtService.java`
2. Inyectarlo en `AuthService`
3. Usarlo en `AuthController.buildLoginResponse()`

**El dominio NO cambia.**

---

## 🎯 Estructura desacoplada para futuro

✅ **Ahora**: Mock InMemory + Fake JWT
🔜 **Futuro 1**: JWT real con jjwt + InMemory
🔜 **Futuro 2**: JWT real + PostgreSQL (JPA)
🔜 **Futuro 3**: OAuth2 + JWT + PostgreSQL

Todos estos cambios se hacen **SIN tocar el dominio**.
