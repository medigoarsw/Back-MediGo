# 📋 Guía: Creación de Base de Datos MediGo en Supabase

## 🎯 Resumen

Se ha creado un **schema SQL completo** que incluye todas las tablas necesarias para el sistema MediGo con:
- ✅ **15 tablas** principales con relaciones bien definidas
- ✅ **30+ índices** para optimización de queries
- ✅ **Datos de prueba** (seed data) listo para usar
- ✅ **Constraints** y validaciones adecuadas
- ✅ **Timestamps** automáticos

---

## 📊 Tablas Incluidas

### 1. **users** - Usuarios del sistema
```sql
Campos:
- id (PK)
- email (UNIQUE, NOT NULL) ⭐ CAMPO PARA AUTENTICACIÓN
- password_hash
- name
- username (para compatibilidad)
- role: ADMIN, AFFILIATE, DELIVERY
- active, phone, created_at, updated_at
```
**Cambio importante:** El email es el campo UNIQUE. Tu login debe cambiar a usar `email` en lugar de `username`.

---

### 2. **branches** - Sucursales/Locales
```sql
Campos:
- id (PK)
- name (UNIQUE)
- address, city, phone
- latitude, longitude (para maps)
- is_active, created_at, updated_at
```

---

### 3. **medications** - Catálogo de medicamentos
```sql
Campos:
- id (PK)
- name (UNIQUE) ⭐
- description, unit
- active, created_at, updated_at
```

---

### 4. **branch_stock** - Inventario por sucursal (CRÍTICA)
```sql
Campos:
- id (PK)
- branch_id (FK), medication_id (FK)
- quantity (CHECK >= 0)
- last_updated (TIMESTAMP)
- UNIQUE(branch_id, medication_id)
```
**Nota:** Esta tabla es crítica para concurrencia. Tiene índices para queries rápidas.

---

### 5. **addresses** - Direcciones de usuarios
```sql
Campos:
- id (PK)
- user_id (FK)
- label: 'Casa', 'Trabajo', etc.
- street, city, neighborhood, postal_code
- latitude, longitude
- is_default, is_active
```
**NUEVA:** Permite guardar múltiples direcciones por usuario.

---

### 6. **orders** - Órdenes/Pedidos
```sql
Campos:
- id (PK)
- affiliate_id (FK), branch_id (FK)
- status: PENDING, CONFIRMED, ASSIGNED, IN_ROUTE, DELIVERED, CANCELLED
- delivery_address_id (FK) O address_lat/lng (backup)
- total_amount, delivery_fee
- notes, created_at, updated_at, delivery_at
```

---

### 7. **order_items** - Items en cada orden
```sql
Campos:
- id (PK)
- order_id (FK), medication_id (FK)
- quantity, unit_price
- subtotal (GENERATED ALWAYS AS)
```

---

### 8. **deliveries** - Entregas/Logística
```sql
Campos:
- id (PK)
- order_id (FK, UNIQUE), delivery_person_id (FK)
- status: ASSIGNED, IN_ROUTE, DELIVERED, FAILED ⭐ (incluyendo FAILED)
- estimated_time, actual_delivery_time
- assigned_at, started_at
- rating (1-5), review
- notes
```

---

### 9. **location_updates** - Rastreo GPS
```sql
Campos:
- id (PK)
- delivery_id (FK)
- latitude, longitude
- accuracy (en metros)
- timestamp (Unix timestamp en ms)
- created_at
```
**NUEVA:** Para tracking real-time de entregas.

---

### 10. **auctions** - Subastas
```sql
Campos:
- id (PK)
- medication_id (FK), branch_id (FK)
- base_price, max_price, inactivity_minutes
- start_time, end_time, last_bid_at
- status: SCHEDULED, ACTIVE, CLOSED, CANCELLED
- closure_type: FIXED_TIME, INACTIVITY, MAX_PRICE
- winner_id (FK), final_price
```

---

### 11. **bids** - Pujas en subastas
```sql
Campos:
- id (PK)
- auction_id (FK), user_id (FK)
- user_name (desnormalizado)
- amount, placed_at
```

---

### 12. **notifications** - Notificaciones
```sql
Campos:
- id (PK)
- user_id (FK)
- type: ORDER_CONFIRMED, DELIVERY_STARTED, etc.
- title, message
- related_id: ID de orden/entrega/subasta
- is_read, created_at
```
**NUEVA:** Para sistema de notificaciones.

---

### 13. **audit_logs** - Auditoría
```sql
Campos:
- id (PK)
- user_id (FK)
- action, entity_type, entity_id
- old_value (JSONB), new_value (JSONB)
- ip_address, user_agent
- created_at
```
**NUEVA:** Para auditoría de cambios.

---

## 🔐 Datos de Prueba Incluidos

### Usuarios (Password: `password123`)
```
admin@medigo.com               → ADMIN
manager@medigo.com             → ADMIN
farmacia.norte@medigo.com      → AFFILIATE
farmacia.sur@medigo.com        → AFFILIATE
farmacia.centro@medigo.com     → AFFILIATE
cliente.juan@medigo.com        → AFFILIATE
cliente.maria@medigo.com       → AFFILIATE
delivery.carlos@medigo.com     → DELIVERY
delivery.diego@medigo.com      → DELIVERY
delivery.ana@medigo.com        → DELIVERY
```

### Sucursales: 5 branches en Bogotá y Soacha
### Medicamentos: 15 medicinas comunes (analgésicos, antihipertensivos, etc.)
### Inventario: Stock inicial para todas las sucursales
### Órdenes: 6 órdenes con diferentes estados
### Entregas: 6 entregas con histórico de ubicación
### Subastas: 6 subastas (2 cerradas, 2 activas, 2 programadas)
### Pujas: 15 pujas distribuidas en las subastas

---

## 🚀 Cómo Ejecutar en Supabase

### Opción 1: SQL Editor (Recomendado)
1. Abre **Supabase Dashboard** → Tu proyecto
2. Ve a **SQL Editor**
3. Clic en **"New Query"**
4. **Copia TODO el contenido** del archivo `database_full_schema.sql`
5. **Pega** en el editor
6. Clic en **"Run"** (botón verde)
7. **Espera** a que complete (puede tomar 10-30 segundos debido a los INSERTs)

### Opción 2: Bash (Script)
```bash
# Si tienes acceso al CLI de Supabase
psql -U postgres -h db.<PROJECT_ID>.supabase.co -d postgres < database_full_schema.sql
```

### Opción 3: Dividido por secciones
Si el script completo falla:
1. Ejecuta primero: **Creación de tablas** (CREATE TABLE statements)
2. Luego: **Índices**
3. Finalmente: **Seed data** (INSERTs)

---

## ⚠️ Cambios Necesarios en el Código Spring Boot

Después de ejecutar el SQL, **debes cambiar tu autenticación**:

### Cambio en `LoginRequestDto.java`
```java
// ANTES:
@Getter @Setter
public class LoginRequestDto {
    private String username;  // ❌ ELIMINAR
    private String password;
}

// DESPUÉS:
@Getter @Setter
public class LoginRequestDto {
    private String email;     // ✅ CAMBIAR A EMAIL
    private String password;
}
```

### Cambio en `AuthService.java`
```java
// ANTES:
public LoginResponseDto login(String username, String password) {
    UserEntity user = userRepository.findByName(username);  // ❌
    // ...
}

// DESPUÉS:
public LoginResponseDto login(String email, String password) {
    UserEntity user = userRepository.findByEmail(email);    // ✅
    // ...
}
```

### Cambio en `AuthController.java`
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequest) {
    // ANTES: authService.login(loginRequest.getUsername(), ...)
    // DESPUÉS:
    return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
}
```

---

## ✅ Verificación

Al final del script SQL, se ejecutan estas queries de verificación:
```sql
SELECT 'Users created' AS check_point, COUNT(*) as total FROM users;
-- Debería mostrar: 10 (usuarios creados)

SELECT 'Orders created', COUNT(*) FROM orders;
-- Debería mostrar: 6 (órdenes creadas)

SELECT 'Medications created', COUNT(*) FROM medications;
-- Debería mostrar: 15 (medicinas creadas)
```

Si ves los números correctos, ¡la DB está lista! ✅

---

## 📌 Próximos Pasos

1. **Ejecuta el SQL en Supabase**
2. **Actualiza el código Spring Boot** (cambios de username → email)
3. **Compila y prueba**:
   ```bash
   mvn clean compile
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
4. **Prueba login en Swagger** con:
   - Email: `admin@medigo.com`
   - Password: `password123`

---

## 🔧 Notas Técnicas

### Índices
- Se crearon **30+ índices** para optimizar queries en las operaciones más comunes
- La tabla `branch_stock` tiene índice composite en (branch_id, medication_id)
- Los searches de orders, auctions y bids están optimizados

### Constraints
- `CHECK` en quantities (>= 0)
- `CHECK` en ratings (1-5)
- `UNIQUE` en email, medication name, branch name
- `ON DELETE CASCADE` para mantener integridad referencial

### Timestamp automáticos
- `created_at` y `updated_at` en la mayoría de tablas
- `last_updated` en `branch_stock`
- `placed_at` en bids

---

## 🆘 Troubleshooting

### Error: "duplicate key value violates unique constraint"
**Causa:** Intentaste ejecutar el script 2 veces.
**Solución:** Elimina todas las tablas primero (o crea un nuevo proyecto en Supabase).

### Error: "foreign key constraint"
**Causa:** Las tablas se crearon en orden incorrecto.
**Solución:** Asegúrate de ejecutar el script **completo** de una sola vez.

### Los INSERTs toman mucho tiempo
**Normal:** Si tienes muchos índices, los INSERTs son más lentos. Espera a que termine.

---

## 📝 Archivo de Referencia

Ubicación: `Back-MediGo/database_full_schema.sql`

Contiene:
- ✅ Schema completo (15 tablas)
- ✅ Índices optimizados
- ✅ Seed data (10 usuarios, 5 sucursales, 15 medicinas, 6 órdenes, etc.)
- ✅ Queries de verificación

---

**¡Todo listo para empezar!** 🎉
