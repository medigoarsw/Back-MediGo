# 🔐 Guía: Cambiar Autenticación de Username a Email

## 📋 Cambios Necesarios

Esta guía te muestra exactamente qué cambios hacer en tu código Spring Boot para que el login funcione con **EMAIL** en lugar de **USERNAME**.

---

## 1️⃣ Cambiar `LoginRequestDto.java`

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/LoginRequestDto.java`

### ❌ ANTES
```java
package edu.escuelaing.arsw.medigo.users.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    private String username;  // ❌ ELIMINAR ESTA LÍNEA
    private String password;
}
```

### ✅ DESPUÉS
```java
package edu.escuelaing.arsw.medigo.users.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    private String email;     // ✅ CAMBIAR A EMAIL
    private String password;
}
```

---

## 2️⃣ Cambiar `AuthService.java`

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/application/service/AuthService.java`

### ❌ ANTES - Método login()
```java
public LoginResponseDto login(String username, String password) {
    // ❌ INCORRECTO: buscar por nombre
    UserEntity user = userRepository.findByName(username);
    
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new AuthenticationException("Credenciales inválidas");
    }
    
    return new LoginResponseDto(user.getId(), user.getEmail(), generateToken(user));
}
```

### ✅ DESPUÉS - Método login()
```java
public LoginResponseDto login(String email, String password) {
    // ✅ CORRECTO: buscar por email
    UserEntity user = userRepository.findByEmail(email);
    
    if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
        throw new AuthenticationException("Credenciales inválidas");
    }
    
    return new LoginResponseDto(user.getId(), user.getEmail(), generateToken(user));
}
```

**Cambios:**
- Parámetro: `username` → `email` ✅
- Método repositorio: `findByName(username)` → `findByEmail(email)` ✅
- El resto del método permanece igual

---

## 3️⃣ Cambiar `AuthController.java`

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/http/AuthController.java`

### ❌ ANTES - Método login()
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
    try {
        // ❌ INCORRECTO: usar getUsername()
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        
        LoginResponseDto response = authService.login(username, password);
        return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(e.getMessage()));
    }
}
```

### ✅ DESPUÉS - Método login()
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
    try {
        // ✅ CORRECTO: usar getEmail()
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        
        LoginResponseDto response = authService.login(email, password);
        return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDto(e.getMessage()));
    }
}
```

**Cambios:**
- `loginRequest.getUsername()` → `loginRequest.getEmail()` ✅
- Variable `username` → `email` ✅

---

## 4️⃣ Verificar `UserRepository.java`

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/persistence/repository/UserRepository.java`

### ✅ DEBE CONTENER

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    // ✅ ESTE MÉTODO YA DEBE EXISTIR
    UserEntity findByEmail(String email);
    
    // ❌ OPCIONAL: Eliminar si no lo necesitas ya
    // UserEntity findByName(String name);
}
```

**Nota:** Si `findByEmail()` no existe, agrégalo:
```java
@Query("SELECT u FROM UserEntity u WHERE u.email = :email")
UserEntity findByEmail(@Param("email") String email);
```

---

## 5️⃣ Cambiar `SignUpRequestDto.java` (IMPORTANTE)

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/SignUpRequestDto.java`

### ✅ DEBE SER ASÍ

```java
package edu.escuelaing.arsw.medigo.users.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {
    private String email;     // ✅ Email para registro
    private String password;  // ✅ Contraseña
    private String name;      // ✅ Nombre completo
    
    // ❌ OPCIONAL: username (no recomendado)
    // private String username;
}
```

---

## 6️⃣ Cambiar `UserResponseDto.java`

**Ubicación:** `src/main/java/edu/escuelaing/arsw/medigo/users/application/dto/UserResponseDto.java`

### ✅ DEBE CONTENER

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;        // ✅ Email del usuario
    private String name;         // ✅ Nombre del usuario
    private String role;         // ✅ Rol (ADMIN, AFFILIATE, DELIVERY)
    private boolean active;      // ✅ Estado activo
    
    // ❌ ELIMINAR SI EXISTE:
    // private String username;
}
```

---

## 7️⃣ Cambiar Tests

### `AuthServiceTest.java`

#### ❌ ANTES
```java
@Test
public void testLoginWithUsername() {
    String username = "testuser";
    String password = "password123";
    
    UserEntity user = new UserEntity();
    user.setName(username);  // ❌ INCORRECTO
    user.setEmail("test@example.com");
    user.setPasswordHash(passwordEncoder.encode(password));
    
    when(userRepository.findByName(username)).thenReturn(user);
    
    LoginResponseDto response = authService.login(username, password);
    assertNotNull(response);
}
```

#### ✅ DESPUÉS
```java
@Test
public void testLoginWithEmail() {
    String email = "test@example.com";
    String password = "password123";
    
    UserEntity user = new UserEntity();
    user.setEmail(email);         // ✅ CORRECTO
    user.setName("Test User");
    user.setPasswordHash(passwordEncoder.encode(password));
    
    when(userRepository.findByEmail(email)).thenReturn(user);
    
    LoginResponseDto response = authService.login(email, password);
    assertNotNull(response);
}
```

---

## 8️⃣ Actualizar Swagger/OpenAPI Docs

Si tienes documentación Swagger, actualiza los ejemplos:

### ❌ ANTES
```yaml
/api/auth/login:
  post:
    requestBody:
      content:
        application/json:
          schema:
            properties:
              username:    # ❌ ELIMINAR
                type: string
              password:
                type: string
```

### ✅ DESPUÉS
```yaml
/api/auth/login:
  post:
    requestBody:
      content:
        application/json:
          schema:
            properties:
              email:       # ✅ CAMBIAR A EMAIL
                type: string
                format: email
                example: "admin@medigo.com"
              password:
                type: string
                example: "password123"
```

---

## 🧪 Prueba los Cambios

### 1️⃣ Compilar
```bash
cd "d:\ander\Documents\SEMESTRE 7\ARSW\PROYECTO OFICIAL\Back-MediGo"
mvn clean compile
```

### 2️⃣ Ejecutar
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 3️⃣ Probar en Swagger

**URL:** http://localhost:8080/swagger-ui.html

**Endpoint:** `POST /api/auth/login`

**Body:**
```json
{
  "email": "admin@medigo.com",
  "password": "password123"
}
```

**Response esperado:**
```json
{
  "id": 1,
  "email": "admin@medigo.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## ✅ Checklist de Cambios

```
[ ] 1. Cambié LoginRequestDto (username → email)
[ ] 2. Cambié AuthService.login() (buscar por email)
[ ] 3. Cambié AuthController.login() (getEmail() en lugar de getUsername())
[ ] 4. Verifiqué UserRepository.findByEmail()
[ ] 5. Cambié SignUpRequestDto si es necesario
[ ] 6. Cambié UserResponseDto
[ ] 7. Actualicé tests (AuthServiceTest, etc.)
[ ] 8. Compilé sin errores: mvn clean compile
[ ] 9. Ejecuté: mvn spring-boot:run
[ ] 10. Probé en Swagger con email y contraseña correcta
```

---

## 🚀 Resumen de Cambios

| Archivo | Cambio | Antes | Después |
|---------|--------|-------|---------|
| `LoginRequestDto` | Campo principal | `username` | `email` |
| `AuthService.login()` | Parámetro | `String username` | `String email` |
| `AuthService.login()` | Búsqueda BD | `findByName()` | `findByEmail()` |
| `AuthController.login()` | Acceso DTO | `getUsername()` | `getEmail()` |
| `AuthServiceTest` | Búsqueda mock | `findByName()` | `findByEmail()` |
| `UserResponseDto` | Campo respuesta | ❌ username | ✅ email |

---

## 🔗 Relaciones en Base de Datos

**IMPORTANTE:** En la BD ya está configurado así:

```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,  -- ✅ EMAIL ÚNICO
    password_hash   VARCHAR(255) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL,
    -- ...
);
```

El email es **UNIQUE NOT NULL**, perfecto para autenticación.

---

## 💡 Dónde Encontrar Estos Archivos

```
Back-MediGo/
├── src/main/java/edu/escuelaing/arsw/medigo/
│   └── users/
│       ├── application/
│       │   ├── dto/
│       │   │   ├── LoginRequestDto.java        ← Cambiar aquí
│       │   │   ├── LoginResponseDto.java
│       │   │   ├── SignUpRequestDto.java       ← Cambiar aquí
│       │   │   └── UserResponseDto.java        ← Cambiar aquí
│       │   └── service/
│       │       └── AuthService.java            ← Cambiar aquí
│       └── infrastructure/
│           ├── http/
│           │   └── AuthController.java         ← Cambiar aquí
│           └── persistence/
│               └── repository/
│                   └── UserRepository.java     ← Verificar aquí
├── src/test/java/...
│   └── AuthServiceTest.java                     ← Actualizar tests aquí
```

---

## ❓ FAQ

**P: ¿Qué pasa si los usuarios tienen un campo `username` también?**
R: Puedes mantenerlo en la BD (incluso como nullable), pero NO lo uses en login. El login siempre debe usar email.

**P: ¿Debo cambiar el modelo de dominio `User.java`?**
R: Opcional. Si tienes un campo `username` en el modelo de dominio que no usas, puedes eliminarlo o dejarlo.

**P: ¿Qué hacer si olvido cambiar un lugar?**
R: El compilador te dará error: "cannot find symbol - getUsername()". Cambia eso a `getEmail()`.

**P: ¿Puedo mantener username como backup?**
R: Sí, pero asegúrate de que NO lo uses en AuthService. El email debe ser el campo de login.

---

## 🤝 Soporte

Si tienes dudas, verifica:
1. ✅ El SQL se ejecutó correctamente en Supabase
2. ✅ La BD tiene el usuario `admin@medigo.com` con password123
3. ✅ Todos los archivos fueron cambiados (usa Buscar en VS Code)
4. ✅ `mvn clean compile` sin errores

---

**Última actualización:** 2 de Abril, 2026
**Estado:** Listo para implementar ✅
