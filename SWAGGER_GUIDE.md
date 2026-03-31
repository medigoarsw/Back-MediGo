# 📚 Guía de Swagger / OpenAPI en MediGo

## 🎯 Descripción

Esta guía explica cómo acceder y utilizar la documentación interactiva de la API mediante Swagger UI (OpenAPI 3.0).

---

## 🚀 Acceso a Swagger UI

Una vez que el servidor está corriendo en `http://localhost:8080`, puedes acceder a Swagger UI en:

### URLs disponibles:

| Recurso | URL |
|---------|-----|
| **Swagger UI (Interactivo)** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |
| **OpenAPI YAML** | http://localhost:8080/v3/api-docs.yaml |

---

## 📖 Estructura de la Documentación

### Autenticación
**Endpoint Tag:** `Authentication`

Los siguientes endpoints están disponibles para autenticación:

#### 1️⃣ **POST /api/auth/login** - Autenticar Usuario
```
Descripción: Autentica un usuario con sus credenciales (username y password)

Request:
{
  "username": "student",
  "password": "123"
}

Response (200 OK):
{
  "access_token": "fake-jwt.1.student.1774978129358",
  "token_type": "Bearer",
  "user_id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "STUDENT",
  "expires_in": 3600
}

Status Codes:
- 200: Autenticación exitosa
- 401: Credenciales inválidas
- 404: Usuario no encontrado
- 500: Error interno del servidor
```

#### 2️⃣ **GET /api/auth/{id}** - Obtener Usuario por ID
```
Descripción: Retorna la información de un usuario específico según su ID

Parámetros:
- id (path): ID del usuario (ejemplo: 1)

Response (200 OK):
{
  "user_id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "STUDENT",
  "active": true
}

Status Codes:
- 200: Usuario encontrado
- 404: Usuario no encontrado
```

#### 3️⃣ **GET /api/auth/email/{email}** - Obtener Usuario por Email
```
Descripción: Retorna la información de un usuario específico según su email

Parámetros:
- email (path): Email del usuario (ejemplo: student@medigo.com)

Response (200 OK):
{
  "user_id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "STUDENT",
  "active": true
}

Status Codes:
- 200: Usuario encontrado
- 404: Usuario no encontrado
```

#### 4️⃣ **GET /api/auth/me** - Obtener Usuario Actual
```
Descripción: Retorna la información del usuario actualmente autenticado

Parámetros:
- user_id (query, requerido): ID del usuario (ejemplo: 1)

Response (200 OK):
{
  "user_id": 1,
  "username": "student",
  "email": "student@medigo.com",
  "role": "STUDENT",
  "active": true
}

Status Codes:
- 200: Usuario encontrado
- 404: Usuario no encontrado
```

---

## 👥 Usuarios de Prueba (In-Memory)

En el MVP actual, los usuarios están almacenados en memoria (InMemoryUserRepository).

### Credenciales disponibles:

| ID | Username | Email | Password | Role |
|----|----------|-------|----------|------|
| 1 | student | student@medigo.com | 123 | STUDENT |
| 2 | admin | admin@medigo.com | 123 | ADMIN |
| 3 | vendor | vendor@medigo.com | 123 | VENDOR |
| 4 | logistics | logistics@medigo.com | 123 | LOGISTICS |

---

## 🧪 Pruebas Interactivas en Swagger UI

### Paso 1: Abrir Swagger UI
Navega a `http://localhost:8080/swagger-ui.html`

### Paso 2: Expandir sección "Authentication"
Haz clic en la sección para ver todos los endpoints.

### Paso 3: Probar el endpoint de login
1. Haz clic en **POST /api/auth/login** → **"Try it out"**
2. En el Request Body, ingresa:
   ```json
   {
     "username": "student",
     "password": "123"
   }
   ```
3. Haz clic en **"Execute"**
4. Verás la respuesta 200 OK con el token

### Paso 4: Probar otros endpoints
1. Haz clic en **GET /api/auth/{id}** → **"Try it out"**
2. Ingresa el ID: `1`
3. Haz clic en **"Execute"**
4. Verás los datos del usuario

---

## 🔒 Seguridad y JWT (Futuro)

Actualmente, en el MVP:
- ✅ Los tokens son "fake tokens" para testing
- ✅ No hay validación de JWT real
- ✅ Los endpoints `/api/auth/**` están abiertos al público

### Cuando se implemente JWT real:
1. Se generarán tokens JWT firmados (HS256 o RS256)
2. Los endpoints protegidos validarán el JWT
3. El esquema de seguridad en Swagger será:
   ```
   Authorization: Bearer <token>
   ```

---

## 📝 DTOs (Data Transfer Objects)

### LoginRequestDto
```json
{
  "username": "string (requerido)",
  "password": "string (requerido)"
}
```

### LoginResponseDto
```json
{
  "access_token": "string",
  "token_type": "string (e.g., Bearer)",
  "user_id": "number",
  "username": "string",
  "email": "string",
  "role": "string",
  "expires_in": "number (segundos)"
}
```

### UserResponseDto
```json
{
  "user_id": "number",
  "username": "string",
  "email": "string",
  "role": "string",
  "active": "boolean"
}
```

---

## 🔗 Archivos Relacionados

- **AuthController.java** - Endpoints REST
- **LoginRequestDto.java** - DTO de entrada
- **LoginResponseDto.java** - DTO de respuesta del login
- **UserResponseDto.java** - DTO de usuario
- **OpenApiConfig.java** - Configuración de Swagger
- **application.properties** - Configuración de la aplicación

---

## 🛠️ Comandos Útiles

### Compilar el proyecto
```bash
mvn clean install
```

### Ejecutar el servidor
```bash
mvn spring-boot:run
```

### Hacer una request HTTP a los endpoints

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student","password":"123"}'
```

**Obtener usuario por ID:**
```bash
curl -X GET http://localhost:8080/api/auth/1
```

**Obtener usuario por email:**
```bash
curl -X GET http://localhost:8080/api/auth/email/student@medigo.com
```

**Obtener usuario actual:**
```bash
curl -X GET "http://localhost:8080/api/auth/me?user_id=1"
```

---

## 🎯 Roadmap

- [ ] Implementar JWT real (HS256 / RS256)
- [ ] Agregar validación de JWT en endpoints
- [ ] Migrar de InMemoryUserRepository a JpaUserRepository (PostgreSQL)
- [ ] Implementar OAuth2 (Google, GitHub)
- [ ] Agregar rate limiting en `/api/auth/login`
- [ ] Documentar otros módulos (Catalog, Orders, Logistics, Auctions)

---

## 💡 Tips

1. **Swagger UI es READ-ONLY** - No modifica efectivamente tus datos desde la UI, pero puedes ver las respuestas
2. **Para requests POST/PUT/DELETE real**, usa Postman, Thunder Client o curl
3. **Los tokens fake se regeneran** cada vez que haces login (incluyen timestamp)
4. **La documentación se genera automáticamente** a partir de las anotaciones `@Operation`, `@Schema`, etc.

---

## 📞 Soporte

Si tienes dudas sobre Swagger o la API, revisa:
- [OpenAPI 3.0 Specification](https://spec.openapis.org/oas/v3.0.3)
- [Springdoc OpenAPI Documentation](https://springdoc.org/)
- [Swagger.io](https://swagger.io/)
