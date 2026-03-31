# 🛣️ Roadmap de Escalada: De MVP a Producción

## Fase 0: MVP Actual (✅ YA IMPLEMENTADO)

```
┌─────────────────────────────────────────────────┐
│         MediGo Authentication MVP                │
├─────────────────────────────────────────────────┤
│ ✅ Autenticación Mock                           │
│ ✅ Usuarios en memoria (HashMap)                │
│ ✅ JWT Fake                                     │
│ ✅ 4 Roles (ADMIN, STUDENT, VENDOR, LOGISTICS) │
│ ✅ Arquitectura Hexagonal                       │
│ ✅ Listo para escalar                           │
└─────────────────────────────────────────────────┘
```

### Usuarios MVP Predefinidos

```
┌──────────┬──────────┬─────────────────────────┬─────────┐
│ Username │ Password │ Email                   │ Role    │
├──────────┼──────────┼─────────────────────────┼─────────┤
│ student  │   123    │ student@medigo.com      │ STUDENT │
│ admin    │   123    │ admin@medigo.com        │ ADMIN   │
│ vendor   │   123    │ vendor@medigo.com       │ VENDOR  │
│ logistics│   123    │ logistics@medigo.com    │ LOGISTICS│
└──────────┴──────────┴─────────────────────────┴─────────┘
```

---

## 📈 Fase 1: JWT Real (1-2 horas)

### Cambios Necesarios

```
ANTES (MVP):
────────────────────────────────
authController.buildLoginResponse()
  └─ String fakeToken = generateFakeJwt(user)

DESPUÉS (Producción):
────────────────────────────────
authController.buildLoginResponse()
  └─ String realToken = jwtService.generateToken(user)
```

### Archivos a Crear/Modificar

```
✏️  NUEVO: JwtService.java
    ├─ generateToken()
    ├─ validateToken()
    └─ extractUserIdFromToken()

✏️  NUEVO: JwtConfig.java
    └─ @Bean JwtService

✏️  MODIFICAR: pom.xml
    └─ <dependency>io.jsonwebtoken:jjwt</dependency>

✏️  MODIFICAR: application.properties
    ├─ jwt.secret=your-secret-key
    └─ jwt.expiration=3600000

✏️  MODIFICAR: AuthController (1 línea)
    └─ Usar jwtService en lugar de generateFakeJwt()

✅ SIN CAMBIOS: AuthService, Domain, UserRepositoryPort
```

### Código Mínimo Necesario

**JwtService.java:**
```java
@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long expiration;
    
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
}
```

**AuthController.java (1 línea cambia):**
```java
// De:
String fakeToken = generateFakeJwt(user);

// A:
String realToken = jwtService.generateToken(user);
```

---

## 🗄️ Fase 2: PostgreSQL (2-3 horas)

### Cambios Necesarios

```
ANTES (MVP - InMemory):
────────────────────────────────
AuthConfig:
  @Bean
  public UserRepositoryPort userRepository() {
    return new InMemoryUserRepository();  ← HashMap en memoria
  }

DESPUÉS (Producción - JPA):
────────────────────────────────
AuthConfig:
  @Bean
  public UserRepositoryPort userRepository(
        UserJpaRepository jpaRepository) {
    return new JpaUserRepository(jpaRepository);  ← PostgreSQL
  }
```

### Archivos a Crear/Modificar

```
✏️  NUEVO: UserEntity.java
    ├─ @Entity, @Table
    ├─ Mapeo a User (toDomain())
    └─ @Enumerated Role

✏️  NUEVO: UserJpaRepository.java
    └─ extends JpaRepository<UserEntity, Long>
       ├─ findByUsername()
       ├─ findByEmail()
       └─ findById()

✏️  NUEVO: JpaUserRepository.java (implements UserRepositoryPort)
    ├─ @Repository
    ├─ @Autowired UserJpaRepository
    └─ Implementa los 3 métodos del puerto

✏️  MODIFICAR: AuthConfig.java (cambiar a JpaUserRepository)

✏️  MODIFICAR: application.properties
    ├─ spring.datasource.url=jdbc:postgresql://...
    ├─ spring.datasource.username=...
    ├─ spring.datasource.password=...
    └─ spring.jpa.hibernate.ddl-auto=update

✏️  MODIFICAR: pom.xml
    ├─ <dependency>org.postgresql:postgresql</dependency>
    └─ (ya está incluido)

✅ SIN CAMBIOS: AuthService, Domain, Controller, Tests
✅ REUTILIZABLE: InMemoryUserRepository (para tests)
```

### Entidad JPA Mínima

**UserEntity.java:**
```java
@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private boolean active = true;
    
    // Mapper a dominio
    public User toDomain() {
        return User.create(id, username, email, password, role);
    }
}
```

**JpaUserRepository.java:**
```java
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepositoryPort {
    
    private final UserJpaRepository jpaRepository;
    
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

---

## 🔒 Fase 3: Spring Security (2-4 horas)

### Cambios Necesarios

```
NUEVO FLUJO CON SPRING SECURITY:
────────────────────────────────
Cliente
  ↓
  POST /api/auth/login
  ↓
  AuthController.login()
  ↓
  AuthService.authenticate()
  ↓
  ✅ Genera JWT
  ↓
  Respuesta con token
  ↓
  
Cliente siguiente request:
  ↓
  GET /api/protected-resource
  Header: Authorization: Bearer <token>
  ↓
  JwtAuthenticationFilter ← 🆕 NUEVO
  ↓
  Valida JWT
  ↓
  ✅ Request permitido
```

### Archivos a Crear/Modificar

```
✏️  NUEVO: JwtAuthenticationFilter.java
    ├─ extends OncePerRequestFilter
    ├─ doFilterInternal()
    └─ Valida JWT en cada request

✏️  NUEVO: JwtExceptionHandler.java
    ├─ @ControllerAdvice
    ├─ handleJwtException()
    └─ handleExpiredToken()

✏️  NUEVO: SecurityConfig.java
    ├─ @EnableWebSecurity
    ├─ @Bean SecurityFilterChain
    ├─ addFilterBefore(JwtAuthenticationFilter)
    └─ Configura rutas públicas/privadas

✏️  MODIFICAR: application.properties
    ├─ spring.security.filter.order=5
    └─ (configuración de CORS si es necesario)

✏️  MODIFICAR: Endpoints que requiren autenticación
    └─ @PostAuthorize, @PreAuthorize

✅ SIN CAMBIOS: AuthService, Domain, Tests
```

### Configuración Mínima

**SecurityConfig.java:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtFilter) throws Exception {
        
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**JwtAuthenticationFilter.java:**
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = extractToken(request);
            
            if (token != null && jwtService.validateToken(token)) {
                String username = jwtService.extractUsername(token);
                // Crear auth context
            }
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") 
            ? header.substring(7) 
            : null;
    }
}
```

---

## 🌐 Fase 4: OAuth2 (Opcional, 4-6 horas)

### Para cuando necesite Google/GitHub login

```
✏️  NUEVO: OAuthController.java
    └─ POST /api/auth/oauth/callback

✏️  NUEVO: OAuthRequest.java (DTO)
    ├─ code (de Google)
    └─ state

✏️  NUEVO: GoogleTokenValidator.java
    ├─ Comunica con Google
    └─ Obtiene datos del usuario

✏️  NUEVO: application.properties (OAuth credentials)
    ├─ google.client-id=...
    ├─ google.client-secret=...
    └─ google.redirect-uri=...

✅ REUTILIZABLE: AuthService (mismo flujo)
✅ SIN CAMBIOS: Domain, tests existentes
```

---

## ⏱️ Estimación de Tiempo Total

```
┌──────────────────────────────────────┐
│ Fase | Tarea          | Tiempo       │
├──────────────────────────────────────┤
│  0   │ MVP (Hecho)    │ ✅ Sistema  │
│  1   │ JWT Real       │ ~1-2 horas  │
│  2   │ PostgreSQL     │ ~2-3 horas  │
│  3   │ Spring Security│ ~2-4 horas  │
│  4   │ OAuth2         │ ~4-6 horas  │
├──────────────────────────────────────┤
│      │ Total (1-3)    │ ~5-9 horas  │
│      │ Total (1-4)    │ ~9-15 horas │
└──────────────────────────────────────┘
```

---

## 🎯 Cambios POR FASE (Resumen)

### Fase 1: JWT Real
- Archivos modificados: 2 (pom.xml, AuthController)
- Archivos creados: 2 (JwtService, JwtConfig)
- Líneas de código: ~100
- **Riesgo**: BAJO

### Fase 2: PostgreSQL
- Archivos modificados: 1 (AuthConfig, properties)
- Archivos creados: 3 (UserEntity, JpaUserRepository, UserJpaRepository)
- Líneas de código: ~300
- **Riesgo**: BAJO (migrations con Liquibase)

### Fase 3: Spring Security
- Archivos creados: 3 (SecurityConfig, JwtFilter, ExceptionHandler)
- Archivos modificados: 1 (properties)
- Líneas de código: ~400
- **Riesgo**: MEDIO (coordinar con otros módulos)

### Fase 4: OAuth2
- Archivos creados: 4 (Controller, Validator, Config, etc.)
- Líneas de código: ~500
- **Riesgo**: MEDIO (deps externas con Google)

---

## 📊 Matriz de Impacto

```
        MVP  JWT  BD   Sec  OAuth
────────────────────────────────
Domain   ✓    ✓    ✓    ✓    ✓     (SIN CAMBIOS)
Service  ✓    ✓    ✓    ✓    ✓     (SIN CAMBIOS)
Control  ✓    ✏️   ✓    ✏️   ✏️    (Mínimos cambios)
Config   ✓    ✏️   ✏️   ✏️   ✏️    (Reconfiguración)
Tests    ✓    ✓    ✓    ✓    ✓     (Nuevos tests)
Props    ✓    ✏️   ✏️   ✏️   ✏️    (Nuevas propiedades)

Leyenda:
✓  = CERO cambios
✏️ = CAMBIOS MÍNIMOS
❌ = REESCRITURA COMPLETA (No aplica)
```

---

## ✅ Checklist para Cada Fase

### Antes de comenzar cada fase:

- [ ] Crear rama feature (`git checkout -b feat/autenticacion-fase-N`)
- [ ] Escribir tests de aceptación
- [ ] Documentar cambios
- [ ] Compilar sin errores: `mvn clean compile`
- [ ] Ejecutar tests: `mvn test`
- [ ] Actualizar README

### Después de completar:

- [ ] Code review
- [ ] Tests passing: `mvn verify`
- [ ] JAR generado: `mvn package`
- [ ] Actualizar ARQUITECTURA.md
- [ ] Crear PR/Merge request

---

## 🎓 Transferencia de Conocimiento

Cada fase debería estar documentada:

1. **ANTES**: Diagrama de flujo actual
2. **CAMBIOS**: Archivos modificados/creados
3. **CÓDIGO**: Snippets de ejemplo
4. **PRUEBA**: Cómo validar que funciona
5. **DESPUÉS**: Diagrama de flujo nuevo

---

## 🚨 Gotchas y Riesgos

### Fase 1 (JWT)
⚠️ **Riesgo**: Expiración del token  
✅ **Solución**: Implementar refresh token en futuro

### Fase 2 (PostgreSQL)
⚠️ **Riesgo**: Migración de datos mock a BD  
✅ **Solución**: Usar Liquibase/Flyway

### Fase 3 (Spring Security)
⚠️ **Riesgo**: Conflictos con CORS (otros módulos)  
✅ **Solución**: Configurar CORS en SecurityConfig

### Fase 4 (OAuth2)
⚠️ **Riesgo**: Credenciales expuestas  
✅ **Solución**: Usar .env o Vault para credenciales

---

## 📞 Soporte Futuro

Cuando implementes cada fase:

1. Revisa este documento
2. Sigue la estructura propuesta
3. Ejecuta los tests
4. Mantén los cambios mínimos
5. Documenta lo nuevo

**El dominio nunca cambia.**  
**AuthService casi nunca cambia.**  
**Solo adaptadores y config.**

---

*Roadmap creado: 2026-03-31*  
*Para proyecto: MediGo MVP*  
*Patrón: Arquitectura Hexagonal*
