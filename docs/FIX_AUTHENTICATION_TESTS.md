# Fix: Autenticación con BCrypt - Actualización de Tests

## Problema Identificado

La integración de CI/CD reportó fallos en tests de autenticación:
```
AuthControllerTest.testLoginSuccess - Expected 200, got 401
AuthControllerTest.testLoginDeliveryRole - Expected 200, got 401  
AuthServiceTest.testAuthenticateSuccess - Credenciales inválidas
AuthServiceTest.testAuthenticateMultipleUsers - Credenciales inválidas
```

## Causa Raíz

Después de cambiar `AuthService.authenticate()` para usar `PasswordEncoder.matches()` (en lugar de `User.credentialsMatch()`), los tests necesitaban actualizarse porque:

1. **InMemoryUserRepository**: Los usuarios de prueba tenían contraseñas en **plaintext** ("123"), pero `passwordEncoder.matches()` espera un **hash bcrypt**
2. **AuthServiceTest**: Los mocks de `PasswordEncoder` no estaban configurados para devolver valores correctos

## Soluciones Implementadas

### 1. Actualizar InMemoryUserRepository

**Archivo**: `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/InMemoryUserRepository.java`

**Cambios**:
- Inyectar `PasswordEncoder` en el constructor
- Hashear las contraseñas de prueba al inicializar usuarios

```java
public InMemoryUserRepository(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
    initializeMockUsers();
}

private void initializeMockUsers() {
    TestDataConfig.TEST_USERS.forEach(testData -> {
        String hashedPassword = passwordEncoder.encode(testData.getPassword());
        User user = User.create(
            testData.getId(),
            testData.getUsername(),
            testData.getEmail(),
            hashedPassword,  // ← Ahora es un hash bcrypt
            testData.getRole()
        );
        addUserToMaps(user);
    });
}
```

### 2. Actualizar AuthConfig

**Archivo**: `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java`

**Cambios**:
- Pasar `PasswordEncoder` al bean de `InMemoryUserRepository`

```java
@Bean
@Profile({"ci", "test"})
public UserRepositoryPort testUserRepository(PasswordEncoder passwordEncoder) {
    return new InMemoryUserRepository(passwordEncoder);  // ← Inyectar passwordEncoder
}
```

### 3. Actualizar AuthServiceTest

**Archivo**: `src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java`

**Cambios**:
- Configurar mocks de `PasswordEncoder` en cada test para comparar plaintext correctamente

```java
@Test
void testAuthenticateSuccess() {
    User user = User.create(1L, "user", "user@example.com", "123", Role.AFFILIATE);
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    
    // ← NUEVO: Configurar mock para plaintext comparison
    when(passwordEncoder.matches("123", "123")).thenReturn(true);
    
    User result = authService.authenticate("user@example.com", "123");
    assertNotNull(result);
}
```

## Resultado

Todos los tests ahora pasan:
- ✅ `AuthControllerTest.testLoginSuccess` - Status 200
- ✅ `AuthControllerTest.testLoginDeliveryRole` - Status 200
- ✅ `AuthServiceTest.testAuthenticateSuccess` - Pasa
- ✅ `AuthServiceTest.testAuthenticateMultipleUsers` - Pasa

## Implicaciones de Seguridad

**This fix ensures**:
1. Passwords are properly hashed with BCrypt in the in-memory repository
2. Plaintext passwords cannot be used directly as login credentials
3. Tests correctly validate the BCrypt-enabled authentication flow
4. The vulnerability where hashed passwords could be sent as plaintext to login is now prevented

## Archivos Modificados

- `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/config/AuthConfig.java`
- `src/main/java/edu/escuelaing/arsw/medigo/users/infrastructure/adapter/out/InMemoryUserRepository.java`
- `src/test/java/edu/escuelaing/arsw/medigo/users/application/service/AuthServiceTest.java`

