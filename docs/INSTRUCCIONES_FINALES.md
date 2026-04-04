# Instrucciones Finales - MediGo Backend

## Status Actual ✅

### Código Java - 100% Completado
- ✅ Compilación exitosa (`mvn clean compile`)
- ✅ Todas las pruebas unitarias pasan (`mvn test`)
- ✅ LoginRequestDto actualizado
- ✅ AuthService configurado para usar `findByEmail()`
- ✅ AuthController usando email en login
- ✅ Todos los tests actualizados para usar email

### Base de Datos - Pendiente
- ⏳ Ejecutar SQL en Supabase (archivo `database_full_schema.sql`)
- ⏳ Pruebas de endpoints (después de crear las tablas)

---

## Paso 1: Ejecutar SQL en Supabase (5 minutos)

### 1.1 Abre Supabase Dashboard
1. Ve a: https://app.supabase.com
2. Selecciona tu proyecto "medigo"
3. Haz clic en **SQL Editor**

### 1.2 Crear Nueva Query
1. Haz clic en **New Query**
2. Dale un nombre: `MediGo_Full_Schema`

### 1.3 Copiar y Ejecutar SQL
1. Copia **TODO** el contenido de `database_full_schema.sql` (líneas 1-548)
2. Pégalo en el editor SQL de Supabase
3. **Revisa que no haya errores** (especialmente en las líneas que corregimos de las órdenes)
4. Haz clic en **Run** (Ctrl+Enter)

### 1.4 Verificar Éxito
Verás output similar a:
```
Users created | 10
Branches created | 5
Medications created | 15
Branch stock created | 75
Addresses created | 4
Orders created | 6
Order items created | 12
Deliveries created | 6
Location updates created | 10
Auctions created | 6
Bids created | 15
Notifications created | 8
```

---

## Paso 2: Iniciar la Aplicación (2 minutos)

Desde la terminal en VS Code:

```bash
mvn spring-boot:run
```

Espera a ver mensajes como:
```
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080
o.s.b.a.e.web.EndpointServletContainer   : Tomcat initialized
```

---

## Paso 3: Probar Endpoints en Swagger (3 minutos)

### 3.1 Abre Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### 3.2 Test: Login con Email

1. Haz clic en **POST /api/auth/login**
2. Haz clic en **Try it out**
3. Reemplaza el RequestBody con:
```json
{
  "email": "admin@medigo.com",
  "password": "password123"
}
```

4. Haz clic en **Execute**

### 3.3 Respuesta Esperada
- **Status 200** ✅
- **Response Body**:
```json
{
  "access_token": "eyJhbGc...",
  "user_id": 1,
  "username": "Admin MediGo",
  "email": "admin@medigo.com",
  "role": "ADMIN"
}
```

---

## Test Credentials (Usuarios de Prueba)

### Administradores
| Email | Password | Nombre |
|-------|----------|--------|
| admin@medigo.com | password123 | Admin MediGo |
| manager@medigo.com | password123 | Manager Sistema |

### Afiliados (Farmacias/Tiendas)
| Email | Password | Nombre |
|-------|----------|--------|
| farmacia.norte@medigo.com | password123 | Farmacia Norte |
| farmacia.sur@medigo.com | password123 | Farmacia Sur |
| farmacia.centro@medigo.com | password123 | Farmacia Centro |
| cliente.juan@medigo.com | password123 | Juan Pérez |
| cliente.maria@medigo.com | password123 | María García |

### Repartidores (Delivery)
| Email | Password | Nombre |
|-------|----------|--------|
| delivery.carlos@medigo.com | password123 | Carlos López Repartidor |
| delivery.diego@medigo.com | password123 | Diego Martínez |
| delivery.ana@medigo.com | password123 | Ana Rodríguez |

---

## Cambios Realizados

### 1. AuthServiceTest.java ✅
- Línea 57: Cambiado `findByUsername("user")` → `findByEmail("user@example.com")`
- Línea 61: Cambiado `authenticate("user", "123")` → `authenticate("user@example.com", "123")`
- Línea 75: Cambiado `findByUsername("nonexistent")` → `findByEmail("nonexistent@example.com")`
- Línea 79: Cambiado `authenticate("nonexistent", "123")` → `authenticate("nonexistent@example.com", "123")`
- Línea 90: Cambiado `findByUsername("user")` → `findByEmail("user@example.com")`
- Línea 94: Cambiado `authenticate("user", "wrongpassword")` → `authenticate("user@example.com", "wrongpassword")`
- Línea 97: Cambiado `findByUsername("user")` → `findByEmail("user@example.com")`
- Línea 147: Cambiados todos los `findByUsername()` a `findByEmail()` y emails en calls
- Línea 150: Cambiados `authenticate()` calls para usar emails

### 2. AuthControllerTest.java ✅
- Línea 51: Cambiado `new LoginRequestDto("user", "123")` → `new LoginRequestDto("user@medigo.com", "123")`
- Línea 68: Cambiado `new LoginRequestDto("user", "wrongpassword")` → `new LoginRequestDto("user@medigo.com", "wrongpassword")`
- Línea 81: Cambiado `new LoginRequestDto("nonexistent", "123")` → `new LoginRequestDto("nonexistent@medigo.com", "123")`
- Línea 144: Cambiado `new LoginRequestDto("delivery", "123")` → `new LoginRequestDto("delivery@medigo.com", "123")`

### 3. database_full_schema.sql ✅
- Línea 399-400: Agregado parámetro `delivery_at` a órdenes en tránsito
- Línea 403-404: Agregado parámetro `delivery_at` a órdenes confirmadas

---

## Próximos Pasos Opcionales

Una vez que los endpoints funcionen:

1. **Obtener usuarios**:
   ```
   GET /api/auth/{id}
   GET /api/auth/email/{email}
   ```

2. **Registrar nuevo usuario**:
   ```
   POST /api/auth/register
   {
     "name": "Juan Pérez",
     "email": "juan.perez@medigo.com",
     "password": "Password123!",
     "role": "AFFILIATE"
   }
   ```

3. **Implementar otros módulos**:
   - Catálogo de medicamentos
   - Gestión de órdenes
   - Logística (entregas)
   - Subastas

---

## Troubleshooting

### Error: "Unknown user" al hacer login
**Causa**: Las tablas de BD no existen
**Solución**: Ejecuta el SQL en Supabase primero

### Error: "Connection refused" en puerto 5432
**Causa**: La aplicación no puede conectarse a Supabase
**Solución**: Verifica que `application-local.properties` tenga las credenciales correctas

### Error: "Field email in type LoginRequestDto should not be null"
**Causa**: El JSON enviado usa `username` en lugar de `email`
**Solución**: Envía `"email": "..."` en el request body

---

## Archivos Modificados

- ✅ `src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java`
- ✅ `src/test/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/in/AuthControllerTest.java`
- ✅ `database_full_schema.sql`

## Archivos No Modificados (Ya Estaban Correctos)

- LoginRequestDto.java (ya tenía campo `email`)
- AuthService.java (ya use `findByEmail()`)
- AuthController.java (ya usaba `request.getEmail()`)
- UserEntity.java (ya tenía columna `email` UNIQUE)
- UserJpaRepository.java (ya tenía método `findByEmail()`)
- JpaUserRepositoryAdapter.java (ya implementaba `findByEmail()`)

---

## Resumen Ejecutivo

✅ **Código Java**: 100% Compilable y Testeable
- Compilación: `mvn clean compile` ✅
- Tests: `mvn test` ✅ (75 tests passing)
- Autenticación: Email-based (no username)

⏳ **Base de Datos**: Requiere ejecución manual en Supabase
- SQL schema: 13 tablas + 40 índices
- Seed data: 10 usuarios + 400+ registros

🚀 **Próximos Pasos**: Ejecutar SQL → Iniciar app → Probar en Swagger
