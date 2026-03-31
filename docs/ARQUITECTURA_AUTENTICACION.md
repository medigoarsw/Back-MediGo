# 🏗️ Módulo de Autenticación - Arquitectura Hexagonal (Ports & Adapters)

## 📋 Estructura del Proyecto

```
users/
├── domain/                          ← DOMINIO (lógica pura de negocio)
│   ├── model/
│   │   └── User.java               ← Entidad de dominio
│   ├── valueobject/
│   │   └── Role.java               ← Value Object inmutable
│   ├── port/
│   │   ├── in/
│   │   │   └── AuthUseCase.java    ← Puerto de ENTRADA (interfaz de caso de uso)
│   │   └── out/
│   │       └── UserRepositoryPort.java ← Puerto de SALIDA (repositorio abstracto)
│   └── exception/
│       ├── UserNotFoundException.java
│       ├── InvalidCredentialsException.java
│       └── DomainException.java
│
├── application/                     ← CAPA DE APLICACIÓN (orquestación)
│   ├── service/
│   │   └── AuthService.java        ← Implementa AuthUseCase
│   ├── command/
│   │   └── LoginCommand.java       ← (Futuro: si necesitas CQRS)
│   └── dto/
│       ├── LoginRequestDto.java    ← Request HTTP → Dominio
│       ├── LoginResponseDto.java   ← Dominio → Response HTTP
│       └── UserResponseDto.java    ← DTO para GET /users/{id}
│
└── infrastructure/                  ← INFRAESTRUCTURA (adaptadores técnicos)
    ├── adapter/
    │   ├── in/
    │   │   └── AuthController.java  ← Adaptador REST (puerto de entrada)
    │   └── out/
    │       ├── InMemoryUserRepository.java   ← Adaptador mock (MVP)
    │       └── JpaUserRepository.java        ← (Futuro: BD real)
    └── config/
        └── AuthConfig.java         ← Inyección de dependencias
```

---

## 🎯 Principios Hexagonales Implementados

### 1. **DOMINIO PURO** ✅
El dominio **NO depende** de:
- Spring Framework
- Bases de datos
- HTTP/REST
- Frameworks externos

El dominio **SOLO contiene**:
- Entidades (`User.java`)
- Reglas de negocio (validaciones, credenciales)
- Excepciones de dominio
- Value Objects (`Role.java`)

```java
// ✅ CORRECTO: Lógica de dominio PURA
public boolean credentialsMatch(String providedPassword) {
    if (!this.active) return false;
    return this.password.equals(providedPassword);
}
```

### 2. **PUERTOS (Interfaces)** ✅
Los puertos definen contratos sin implementación:

**PUERTO DE ENTRADA** (`AuthUseCase.java`):
```java
public interface AuthUseCase {
    User authenticate(String username, String password);
    User getUserById(Long userId);
    User getUserByEmail(String email);
}
```

**PUERTO DE SALIDA** (`UserRepositoryPort.java`):
```java
public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
}
```

### 3. **ADAPTADORES** ✅
Los adaptadores implementan los puertos:

**ADAPTADOR DE ENTRADA** (`AuthController.java`):
- Recibe HTTP
- Inyecta `AuthUseCase`
- Convierte DTOs → Dominio
- Convierte Dominio → DTOs

**ADAPTADOR DE SALIDA** (`InMemoryUserRepository.java`):
- Implementa `UserRepositoryPort`
- Almacena usuarios en `HashMap`
- Puede ser reemplazado sin cambiar dominio

### 4. **INYECCIÓN DE DEPENDENCIAS** ✅
```java
@Configuration
public class AuthConfig {
    
    @Bean
    public UserRepositoryPort userRepository() {
        // MVP: InMemory
        return new InMemoryUserRepository();
        
        // Futuro: Solo cambias esta línea
        // return new JpaUserRepository(jpaRepository);
    }
}
```

---

## 🚀 Cómo Funciona el Flujo

### LOGIN: `POST /api/auth/login`

```
[HTTP Request]
    ↓
[AuthController.login()]
    ↓
[AuthService.authenticate()]      ← Implementa AuthUseCase
    ↓
[User.credentialsMatch()]         ← Lógica pura de dominio
    ↓
[InMemoryUserRepository]          ← Busca el usuario (MVP)
    ↓
[User object]                     ← Devuelve Model de dominio
    ↓
[AuthController buildLoginResponse()] ← Convierte a DTO
    ↓
[HTTP Response + Fake JWT]
```

---

## 📱 Endpoints Disponibles

### 1. **POST `/api/auth/login`**
Autentica un usuario

**Request:**
```json
{
  "username": "user",
  "password": "123"
}
```

**Response (200 OK):**
```json
{
  "access_token": "fake-jwt.1.user.1711894234567",
  "token_type": "Bearer",
  "user_id": 2,
  "username": "user",
  "email": "user@medigo.com",
  "role": "USER",
  "expires_in": 3600
}
```

### 2. **GET `/api/auth/me?user_id={id}`**
Obtiene el perfil del usuario

**Response (200 OK):**
```json
{
  "id": 2,
  "username": "user",
  "email": "user@medigo.com",
  "role": "USER",
  "active": true
}
```

### 3. **GET `/api/auth/{id}`**
Obtiene usuario por ID

### 4. **GET `/api/auth/email/{email}`**
Obtiene usuario por email

---

## 👥 Usuarios de Prueba (MVP)

Inicializados automáticamente en `InMemoryUserRepository`:

| Username | Password | Email | Role |
|----------|----------|-------|------|
| `admin` | `123` | admin@medigo.com | ADMIN |
| `user` | `123` | user@medigo.com | USER |
| `delivery` | `123` | delivery@medigo.com | DELIVERY |

**Explicación de roles:**
- **ADMIN**: Empresa Promotora de Salud (EPS) - permisos administrativos
- **USER**: Usuario regular o paciente - acceso básico
- **DELIVERY**: Repartidor de medicamentos - acceso para entregas

---

## 🔄 Cómo Escalar: Migración a JWT Real

### PASO 1: Crear `JwtService`

```java
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole().getCode())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
}
```

### PASO 2: Modificar `AuthController`

```java
@Service
public class AuthService implements AuthUseCase {
    
    @Autowired
    private JwtService jwtService;  // ← Inyecta JWT Service
    
    // El resto del código NO cambia
}
```

**En `AuthController.buildLoginResponse()`:**

```java
// De:
String fakeToken = generateFakeJwt(user);

// A:
String realToken = jwtService.generateToken(user);
```

### PASO 3: Agregar dependen cia en `pom.xml`

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.11.5</version>
</dependency>
```

✅ **¡Dominio y casos de uso NO se tocan!**

---

## 🗄️ Cómo Escalar: Migración a PostgreSQL

### PASO 1: Crear Entity JPA

```java
@Entity
@Table(name = "users")
@Getter @Setter
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private boolean active;
    
    // Mapper a dominio
    public User toDomain() {
        return User.create(id, username, email, password, role);
    }
}
```

### PASO 2: Crear JPA Repository

```java
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
}
```

### PASO 3: Crear Adaptador JPA

```java
@Repository
public class JpaUserRepository implements UserRepositoryPort {
    
    @Autowired
    private UserJpaRepository jpaRepository;
    
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
            .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
            .map(UserEntity::toDomain);
    }
}
```

### PASO 4: Modificar `AuthConfig.java`

```java
@Configuration
public class AuthConfig {
    
    @Bean
    public UserRepositoryPort userRepository(UserJpaRepository jpaRepository) {
        // De InMemory:
        // return new InMemoryUserRepository();
        
        // A JPA:
        return new JpaUserRepository(jpaRepository);
    }
}
```

✅ **¡AuthService, AuthController, Domain NO cambian!**

---

## ✨ Ventajas de esta Arquitectura

| Beneficio | Explicación |
|-----------|-------------|
| 🔓 **Desacoplamiento** | Cambiar de InMemory a JPA solo modifica un `@Bean` |
| 🧪 **Testeable** | Sin dependencias de BD, fácil hacer unit tests |
| 🔄 **Escalable** | Agregar OAuth, LDAP, 2FA sin romper nada |
| 📚 **Limpio** | Separación clara de responsabilidades |
| 🚀 **Flexible** | Cambiar REST → GraphQL → gRPC sin afectar dominio |

---

## 🧪 Testing (Próximo Paso)

### Unit Test del Dominio

```java
@Test
public void testUserCredentialsMatch() {
    User user = User.create(1L, "student", 
        "student@medigo.com", "123", Role.STUDENT);
    
    assertTrue(user.credentialsMatch("123"));
    assertFalse(user.credentialsMatch("wrong"));
}
```

### Integration Test

```java
@SpringBootTest
public class AuthControllerTest {
    
    @Autowired
    private MockMvc mvc;
    
    @Test
    public void testLoginSuccess() throws Exception {
        mvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"student\",\"password\":\"123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists());
    }
}
```

---

## 📝 Resumen

✅ **MVP actual**: Login mock en memoria, JWT fake
✅ **Arquitectura**: Hexagonal completa con puertos y adaptadores
✅ **Escalable**: Cambio a JWT real en 2 pasos
✅ **BD-Ready**: Cambio a PostgreSQL en 4 pasos
✅ **Desacoplado**: Dominio no sabe nada de REST, BD, JWT

🔥 **Esta arquitectura es EXACTAMENTE lo que mencionaste en tu documento MediGo**
