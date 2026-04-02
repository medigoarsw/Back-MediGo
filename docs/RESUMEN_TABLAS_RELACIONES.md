# 🗄️ Resumen: Base de Datos MediGo - Tablas y Relaciones

## 📊 Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CORE TABLES (NÚCLEO)                       │
└─────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │    USERS     │  (emails únicos, 3 roles)
    │              │  ├── id, email ⭐, password_hash
    │              │  ├── name, role (ADMIN/AFFILIATE/DELIVERY)
    │              │  └── active, phone, timestamps
    └──────────────┘
         │      ▲
         │      └────────────────────┐
         │                           │
         ▼                           │
    ┌──────────────┐                 │
    │  ADDRESSES   │  (direcciones de usuarios)
    │              │  ├── user_id (FK)
    │              │  ├── label, street, city, neighborhood
    │              │  └── is_default, is_active
    └──────────────┘

    ┌──────────────┐
    │   BRANCHES   │  (sucursales/locales)
    │              │  ├── id, name, address, city
    │              │  └── latitude, longitude, phone
    └──────────────┘
         ▲              ▲
         │              │
    ┌────┴──────────────┴────┐
    │   BRANCH_STOCK         │  (inventario crítico)
    │   (table concurrente)  │
    │  ├── branch_id (FK)    │
    │  ├── medication_id (FK)│
    │  └── quantity          │  (CHECK >= 0)
    └────┬──────────────┬────┘
         │              │
         │              ▼
         │         ┌──────────────┐
         │         │ MEDICATIONS  │  (catálogo)
         │         │              │  ├── id, name (UNIQUE) ⭐
         │         │              │  ├── description, unit
         │         │              │  └── active
         │         └──────────────┘
         │


┌─────────────────────────────────────────────────────────────────────┐
│                   ORDERS & DELIVERY (PEDIDOS Y ENTREGAS)           │
└─────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │    ORDERS    │  (órdenes/pedidos)
    │              │  ├── affiliate_id (FK → users) ⭐
    │              │  ├── branch_id (FK → branches)
    │              │  ├── status (PENDING, CONFIRMED, IN_ROUTE, DELIVERED, etc.)
    │              │  ├── delivery_address_id (FK → addresses)
    │              │  └── total_amount, delivery_fee, notes
    └──────────────┘
         ▲  │
         │  ▼
         │  ┌──────────────┐
         │  │ ORDER_ITEMS  │  (items en la orden)
         │  │              │  ├── order_id (FK)
         │  │              │  ├── medication_id (FK)
         │  │              │  ├── quantity, unit_price
         │  │              │  └── subtotal (GENERATED)
         │  └──────────────┘
         │
         └─────────────────────┬────────┐
                               │        │
                               ▼        ▼
                          ┌──────────────────┐
                          │   DELIVERIES     │  (entregas)
                          │                  │  ├── order_id (FK, UNIQUE)
                          │                  │  ├── delivery_person_id (FK → users)
                          │                  │  ├── status (ASSIGNED, IN_ROUTE, DELIVERED, FAILED)
                          │                  │  ├── estimated_time, actual_delivery_time
                          │                  │  ├── rating, review
                          │                  │  └── notes
                          └──────────────────┘
                               │
                               ▼
                         ┌──────────────────┐
                         │ LOCATION_UPDATES │  (GPS tracking)
                         │                  │  ├── delivery_id (FK)
                         │                  │  ├── latitude, longitude
                         │                  │  ├── accuracy
                         │                  │  └── timestamp (Unix ms)
                         └──────────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                       AUCTIONS (SUBASTAS)                           │
└─────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │   AUCTIONS   │  (subastas)
    │              │  ├── medication_id (FK)
    │              │  ├── branch_id (FK)
    │              │  ├── base_price, max_price
    │              │  ├── inactivity_minutes
    │              │  ├── start_time, end_time, last_bid_at
    │              │  ├── status (SCHEDULED, ACTIVE, CLOSED, CANCELLED)
    │              │  ├── closure_type (FIXED_TIME, INACTIVITY, MAX_PRICE)
    │              │  ├── winner_id (FK → users)
    │              │  └── final_price
    └──────────────┘
         │
         ▼
    ┌──────────────┐
    │    BIDS      │  (pujas)
    │              │  ├── auction_id (FK)
    │              │  ├── user_id (FK → users)
    │              │  ├── amount
    │              │  └── placed_at
    └──────────────┘


┌─────────────────────────────────────────────────────────────────────┐
│                  SUPPORT TABLES (TABLAS DE SOPORTE)                 │
└─────────────────────────────────────────────────────────────────────┘

    ┌──────────────────────────────────────────────────┐
    │  NOTIFICATIONS                                   │
    │  ├── user_id (FK)                               │
    │  ├── type (ORDER_CONFIRMED, DELIVERY_STARTED...)│
    │  ├── title, message                              │
    │  ├── related_id (ID de orden/entrega/subasta)   │
    │  └── is_read                                     │
    └──────────────────────────────────────────────────┘

    ┌──────────────────────────────────────────────────┐
    │  AUDIT_LOGS                                      │
    │  ├── user_id (FK)                               │
    │  ├── action (CREATE_ORDER, UPDATE_STATUS...)    │
    │  ├── entity_type, entity_id                     │
    │  ├── old_value (JSONB), new_value (JSONB)       │
    │  └── ip_address, user_agent                     │
    └──────────────────────────────────────────────────┘
```

---

## 📈 Estadísticas de Seed Data

| Entidad | Cantidad | Descripción |
|---------|----------|-------------|
| **Users** | 10 | 2 ADMIN, 5 AFFILIATE, 3 DELIVERY |
| **Branches** | 5 | Bogotá (4) + Soacha (1) |
| **Medications** | 15 | Variedad de medicamentos comunes |
| **Branch Stock Entries** | 75 | Stock inicial para cada sucursal |
| **Addresses** | 4 | 2 para Juan Pérez, 2 para María García |
| **Orders** | 6 | Estados: PENDING(0), CONFIRMED(2), IN_ROUTE(2), DELIVERED(2) |
| **Order Items** | 12 | ~2 medicinas por orden |
| **Deliveries** | 6 | 2 DELIVERED con calificaciones, 2 IN_ROUTE, 2 ASSIGNED |
| **Location Updates** | 10 | Histórico GPS de entregas |
| **Auctions** | 6 | 2 CLOSED, 2 ACTIVE, 2 SCHEDULED |
| **Bids** | 15 | ~2.5 pujas por subasta |
| **Notifications** | 10 | Variedad de tipos |

---

## 🔑 Claves de Acceso para Pruebas

### Usuarios Admin
```
Email: admin@medigo.com
Pass:  password123
Role:  ADMIN
```

### Usuarios Affiliate (Clientes/Farmacias)
```
Email: cliente.juan@medigo.com
Pass:  password123
Role:  AFFILIATE

Email: farmacia.norte@medigo.com
Pass:  password123
Role:  AFFILIATE
```

### Usuarios Delivery (Repartidores)
```
Email: delivery.carlos@medigo.com
Pass:  password123
Role:  DELIVERY
```

---

## 📌 Índices Creados (30+)

### Por Tabla
- **users**: 4 índices (email, role, active, created_at)
- **branches**: 2 índices (is_active, city)
- **medications**: 2 índices (name, active)
- **branch_stock**: 2 índices (branch_id, medication_id)
- **addresses**: 2 índices (user_id, is_default)
- **orders**: 4 índices (affiliate, branch, status, composite)
- **order_items**: 2 índices (order_id, medication_id)
- **deliveries**: 4 índices (order_id, person, status, created_at)
- **location_updates**: 2 índices (delivery_id, timestamp)
- **auctions**: 5 índices (status, branch, medication, times)
- **bids**: 4 índices (auction, user, composite, placed_at)
- **notifications**: 3 índices (user_id, read flag, created_at)
- **audit_logs**: 3 índices (user_id, entity, created_at)

**Total: 40+ índices para máximo performance** ⚡

---

## ⚠️ **CAMBIOS URGENTES EN CÓDIGO SPRING BOOT**

La autenticación actual usa `username`, pero **debe cambiar a `email`**:

### 🔴 ANTES (Incorrecto)
```java
// LoginRequestDto
public class LoginRequestDto {
    private String username;  // ❌ ELIMINAR
    private String password;
}

// AuthService
public LoginResponseDto login(String username, String password) {
    UserEntity user = userRepository.findByName(username);  // ❌ INCORRECTO
}

// AuthController
authService.login(loginRequest.getUsername(), ...);  // ❌ INCORRECTO
```

### 🟢 DESPUÉS (Correcto)
```java
// LoginRequestDto
public class LoginRequestDto {
    private String email;     // ✅ CAMBIAR A EMAIL
    private String password;
}

// AuthService
public LoginResponseDto login(String email, String password) {
    UserEntity user = userRepository.findByEmail(email);  // ✅ CORRECTO
}

// AuthController
authService.login(loginRequest.getEmail(), ...);  // ✅ CORRECTO
```

---

## 🚀 Plan de Implementación

### PASO 1: Ejecutar SQL en Supabase
```
⏱️ Tiempo: 5 minutos
- Abre Supabase Dashboard
- SQL Editor → New Query
- Copia database_full_schema.sql
- Ejecuta
```

### PASO 2: Actualizar Código
```
⏱️ Tiempo: 10 minutos
- Cambiar LoginRequestDto: username → email
- Cambiar AuthService.login(): buscar por email
- Cambiar AuthController: mapear email en DTO
- Cambiar los repositorios si necesario
```

### PASO 3: Compilar y Probar
```bash
⏱️ Tiempo: 5 minutos
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# En Swagger:
# POST /api/auth/login
# Body: { "email": "admin@medigo.com", "password": "password123" }
```

---

## ✅ Checklist Final

- [ ] Ejecuté el SQL en Supabase
- [ ] Verifiqué que creó 13 tablas
- [ ] Cambié `LoginRequestDto` a usar `email`
- [ ] Cambié `AuthService.login()` a buscar por email
- [ ] Actualicé `AuthController` 
- [ ] Compilé: `mvn clean compile`
- [ ] Ejecuté: `mvn spring-boot:run`
- [ ] Probé login con `admin@medigo.com` / `password123`
- [ ] ✅ Sistema funcionando

---

**Documentos de referencia:**
- `database_full_schema.sql` - Schema completo con seed data
- `GUIA_BASE_DATOS.md` - Instrucciones detalladas
- `RESUMEN_TABLAS_RELACIONES.md` - Este archivo

**¡Listo para implementar!** 🎉
