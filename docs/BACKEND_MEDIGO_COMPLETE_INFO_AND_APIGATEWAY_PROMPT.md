# 🚀 INFORMACIÓN COMPLETA BACKEND MEDIGO + PROMPT APIGATEWAY

## 📋 TABLA DE CONTENIDOS

1. [Stack Tecnológico](#stack-tecnológico)
2. [Módulos Implementados](#módulos-implementados)
3. [Todos los Endpoints](#todos-los-endpoints)
4. [PROMPT para APIGATEWAY](#prompt-para-apigateway)
5. [Historias de Usuario Completadas](#historias-de-usuario-completadas)

---

## 🛠️ Stack Tecnológico

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| **Framework** | Spring Boot | 3.1.5 |
| **Java** | JDK | 21 |
| **Seguridad** | Spring Security | 6 |
| **Autenticación** | JWT | JJWT |
| **ORM** | JPA/Hibernate | Incluido en Spring Boot |
| **BD Producción** | PostgreSQL | 12+ |
| **BD Testing** | H2 | In-memory |
| **Testing** | JUnit 5 + Mockito | Incluido |
| **API Docs** | Swagger/OpenAPI | 3.0 |
| **Build** | Maven | 3.9+ |
| **Server** | Apache Tomcat | Incluido en Spring Boot |

### Dependencias Clave (pom.xml)
```xml
<dependencias>
  - spring-boot-starter-web (REST)
  - spring-boot-starter-security (Autenticación)
  - spring-boot-starter-data-jpa (ORM)
  - spring-boot-starter-validation (@Valid)
  - spring-boot-starter-data-redis-reactive (Redis para caché/rate limit)
  - spring-boot-starter-websocket (WebSocket para entregas en tiempo real)
  - springdoc-openapi (Swagger/OpenAPI)
  - postgresql (Driver BD)
  - h2 (BD de testing)
  - jjwt (JWT)
  - lombok (Reduce boilerplate)
</dependencias>
```

---

## 🏗️ ARQUITECTURA: Hexagonal

```
┌─────────────────────────────────────────────────────┐
│             ADAPTADORES DE ENTRADA                  │
│  (Controllers REST - Reciben peticiones HTTP)       │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌───────┐   │
│  │ AuthCtrl     │  │ MedicationCtrl│ │OrderCtrl  │
│  │ (:8080)      │  │ (:8080)      │  │ (:8080)   │
│  └──────┬───────┘  └──────┬───────┘  └───┬───┘   │
│         │                 │                │      │
├─────────┼─────────────────┼────────────────┼──────┤
│             PUERTOS/CASOS DE USO                   │
│  (Interfaces que definen contrato de negocio)     │
├─────────┼─────────────────┼────────────────┼──────┤
│         │                 │                │      │
│  ┌──────▼────┐  ┌────────▼──┐  ┌────────▼┐    │
│  │Auth       │  │Search      │  │Create   │    │
│  │UseCase    │  │Medication  │  │Order    │    │
│  │(Interface)│  │UseCase     │  │UseCase  │    │
│  └──────┬────┘  └────────┬──┘  └────────┬┘    │
│         │                 │                │      │
├─────────┼─────────────────┼────────────────┼──────┤
│            SERVICIOS / LÓGICA DE NEGOCIO          │
│  (Implementan UseCases, contienen reglas)        │
├─────────┼─────────────────┼────────────────┼──────┤
│         │                 │                │      │
│  ┌──────▼────┐  ┌────────▼──┐  ┌────────▼┐    │
│  │AuthService│  │CatalogSvc │  │OrderSvc │    │
│  └──────┬────┘  └────────┬──┘  └────────┬┘    │
│         │                 │                │      │
├─────────┼─────────────────┼────────────────┼──────┤
│           ADAPTADORES DE SALIDA (BD)             │
│  (Repositories JPA - Acceso a datos)             │
├─────────┼─────────────────┼────────────────┼──────┤
│         │                 │                │      │
│    PostgreSQL / H2 (Testing) / Redis Cache      │
└─────────┴─────────────────┴────────────────┴──────┘
```

---

## 📦 MÓDULOS IMPLEMENTADOS

### 1️⃣ MÓDULO DE USUARIOS (`/users`)

**Ubicación del código**: `src/main/java/.../users/`

**Componentes**:
- `AuthController` - REST entrada
- `AuthService` - Lógica
- `UserJpaRepository` - BD

**Historias de Usuario**:
- HU-01: Login de Usuario
- HU-02: Autenticación JWT

**Estados de Usuario**:
- `CLIENTE` - Puede comprar medicamentos
- `REPARTIDOR` - Puede hacer entregas
- `ADMIN` - Puede crear/editar medicamentos y subastas

---

### 2️⃣ MÓDULO DE CATÁLOGO (`/catalog`)

**Ubicación del código**: `src/main/java/.../catalog/`

**Componentes**:
- `MedicationController` - REST entrada
- `CatalogService` - Lógica
- `SearchMedicationUseCase` - Caso de uso búsqueda
- `CreateMedicationUseCase` - Caso de uso crear (HU-07)
- `UpdateStockUseCase` - Caso de uso editar stock (HU-08)
- `MedicationJpaRepository` - BD Medicamentos
- `BranchStockJpaRepository` - BD Stock por sucursal

**Historias de Usuario**:
- HU-03: Búsqueda de Medicamentos
- HU-07: Crear Medicamento (ADMIN)
- HU-08: Editar Stock (ADMIN)

**Tablas BD**:
- `medications` - Catálogo
- `branch_stock` - Stock por sucursal (branchId + medicationId)
- `branches` - Sucursales

---

### 3️⃣ MÓDULO DE ÓRDENES (`/orders`)

**Ubicación del código**: `src/main/java/.../orders/`

**Componentes**:
- `OrderController` - REST entrada
- `OrderService` - Lógica
- `CreateOrderUseCase` - Caso de uso crear
- `ConfirmOrderUseCase` - Caso de uso confirmar
- `OrderJpaRepository` - BD Órdenes
- `OrderItemJpaRepository` - BD Ítems (líneas de orden)

**Historias de Usuario**:
- HU-04: Creación de Pedidos (carrito)
- HU-05: Confirmación de Órdenes
- HU-06: Asignación de Ruta de Entrega

**Estados de Orden**:
- `PENDING` - Carrito en progreso
- `CONFIRMED` - Orden confirmada, pendiente envío
- `PENDING_SHIPPING` - En proceso de asignación
- `ASSIGNED` - Repartidor asignado
- `IN_ROUTE` - En camino
- `DELIVERED` - Entregado
- `PENDING_PAYMENT` - Pendiente de pago (legacy)
- `CANCELLED` - Cancelado

**Tablas BD**:
- `orders` - Órdenes principales
- `order_items` - Líneas de orden (medicamentos en la orden)

---

### 4️⃣ MÓDULO DE LOGÍSTICA (`/logistics`)

**Ubicación del código**: `src/main/java/.../logistics/`

**Componentes**:
- `LogisticsController` - REST entrada
- `LogisticsService` - Lógica
- `AssignDeliveryUseCase` - Caso de uso asignación + completar (HU-10)
- `GetActiveDeliveriesUseCase` - Caso de uso entregas activas (HU-11)
- `DeliveryJpaRepository` - BD Entregas

**Historias de Usuario**:
- HU-10: Actualización Automática a Estado "Entregado"
- HU-11: Repartidor Presiona Botón de Finalizar Entrega

**Estados de Entrega**:
- `ASSIGNED` - Asignada al repartidor
- `IN_ROUTE` - En ruta
- `DELIVERED` - Entregada

**Tablas BD**:
- `deliveries` - Registro de entregas
- `orders` - Referencia a orden (orden ha sido entregada)

---

### 5️⃣ MÓDULO DE SUBASTAS (`/auction`)

**Ubicación del código**: `src/main/java/.../auction/`

**Componentes**:
- `AuctionController` - REST entrada
- `AuctionService` - Lógica
- `AuctionJpaRepository` - BD Subastas
- `BidJpaRepository` - BD Pujas

**Historias de Usuario**:
- HU-15: Crear Subasta (ADMIN)
- HU-16: Editar Subasta (ADMIN)
- HU-17: Ver Detalle de Subasta
- HU-18: Unirse a Subasta
- HU-19: Realizar Puja
- HU-22: Consultar Ganador

**Estados de Subasta**:
- `SCHEDULED` - Programada (no ha iniciado)
- `ACTIVE` - En proceso
- `CLOSED` - Finalizada
- `CANCELLED` - Cancelada

**Tipo de Cierre**:
- `FIXED_TIME` - Cierra a hora específica
- `INACTIVITY` - Cierra después de X minutos sin pujas

**Tablas BD**:
- `auctions` - Subastas
- `bids` - Pujas (usuario, cantidad, timestamp)
- `medications` - Referencia (medicamento subastado)

---

## 📡 TODOS LOS ENDPOINTS

### 🔓 ENDPOINTS SIN AUTENTICACIÓN (Públicos)

```
┌─ USUARIOS
│  POST   /api/auth/login                [Sin JWT]
│  POST   /api/auth/register             [Sin JWT]
│
├─ CATÁLOGO
│  GET    /api/medications/search?name=X [Sin JWT]
│  GET    /api/medications/branch/{branchId}/stock [Sin JWT]
│  GET    /api/medications/branch/{branchId}/medications [Sin JWT]
│  GET    /api/medications/branches [Sin JWT]
│
└─ (Otros requieren JWT)
```

---

### 🔐 ENDPOINTS CON AUTENTICACIÓN JWT (Protegidos)

#### 👤 AUTENTICACIÓN (/api/auth)

| Método | Endpoint | Descripción | Rol Requerido |
|--------|----------|-------------|---------------|
| GET | `/api/auth/me` | Obtener perfil actual | CLIENTE, REPARTIDOR, ADMIN |
| GET | `/api/auth/{id}` | Obtener usuario por ID | CLIENTE, REPARTIDOR, ADMIN |
| GET | `/api/auth/email/{email}` | Obtener usuario por email | CLIENTE, REPARTIDOR, ADMIN |

**Headers**: `Authorization: Bearer <JWT_TOKEN>`

---

#### 📚 CATÁLOGO (/api/medications)

| Método | Endpoint | Descripción | Rol | HU |
|--------|----------|-------------|-----|-----|
| POST | `/api/medications` | Crear medicamento | ADMIN | HU-07 |
| PUT | `/api/medications/{id}/stock` | Editar stock | ADMIN | HU-08 |
| PUT | `/api/medications/{id}` | Actualizar medicamento (completo) | ADMIN | HU-07 |
| GET | `/api/medications/{id}` | Obtener medicamento por ID | CLIENTE, REPARTIDOR, ADMIN | - |

**Headers**: `Authorization: Bearer <JWT_TOKEN>` (excepto GET /search)

---

#### 🛒 ÓRDENES (/api/orders)

| Método | Endpoint | Descripción | Rol | HU |
|--------|----------|-------------|-----|-----|
| POST | `/api/orders/cart/add` | Agregar medicamento al carrito | CLIENTE | HU-04 |
| GET | `/api/orders/cart` | Obtener carrito actual | CLIENTE | HU-04 |
| DELETE | `/api/orders/cart/{cartId}/{medicationId}` | Eliminar medicamento del carrito | CLIENTE | HU-04 |
| POST | `/api/orders/confirm` | Confirmar orden | CLIENTE | HU-05 |
| GET | `/api/orders/{orderId}` | Obtener detalle de orden | CLIENTE | HU-05 |
| GET | `/api/orders/affiliate/{affiliateId}` | Listar órdenes del cliente | CLIENTE | - |

**Headers**: `Authorization: Bearer <JWT_TOKEN>`

---

#### 📦 LOGÍSTICA (/api/logistics)

| Método | Endpoint | Descripción | Rol | HU |
|--------|----------|-------------|-----|-----|
| PUT | `/api/logistics/deliveries/{id}/complete` | Confirmar entrega | REPARTIDOR | HU-10 |
| GET | `/api/logistics/deliveries/active` | Entregas activas del repartidor | REPARTIDOR | HU-11 |
| GET | `/api/logistics/deliveries/{id}` | Obtener detalle entrega | REPARTIDOR | HU-11 |
| PUT | `/api/logistics/deliveries/{id}/location` | Actualizar ubicación GPS | REPARTIDOR | - |

**Headers**: `Authorization: Bearer <JWT_TOKEN>`  
**Parámetros Query**: `deliveryPersonId` (para validar propiedad)

---

#### 🎯 SUBASTAS (/api/auctions)

| Método | Endpoint | Descripción | Rol | HU |
|--------|----------|-------------|-----|-----|
| POST | `/api/auctions` | Crear subasta | ADMIN | HU-15 |
| PUT | `/api/auctions/{id}` | Editar subasta | ADMIN | HU-16 |
| GET | `/api/auctions/{id}` | Obtener detalle subasta | CLIENTE, REPARTIDOR, ADMIN | HU-17 |
| GET | `/api/auctions/active` | Listar subastas activas | CLIENTE, REPARTIDOR, ADMIN | HU-17 |
| GET | `/api/auctions/{id}/bids` | Historial de pujas | CLIENTE, REPARTIDOR, ADMIN | HU-17 |
| POST | `/api/auctions/{id}/join` | Unirse a subasta | CLIENTE | HU-18 |
| POST | `/api/auctions/{id}/bids` | Realizar puja | CLIENTE | HU-19 |
| GET | `/api/auctions/{id}/winner` | Obtener ganador | CLIENTE, REPARTIDOR, ADMIN | HU-22 |

**Headers**: `Authorization: Bearer <JWT_TOKEN>`

---

## 🚀 PROMPT PARA APIGATEWAY

### CONTEXTO GENERAL

El APIGATEWAY es un **proxy inteligente** que:
- ✅ Redirige TODAS las peticiones `/api/*` al Backend MediGo (:8080)
- ✅ Valida JWT en cada petición (excepto login/register)
- ✅ Aplica rate limiting (Redis)
- ✅ Audita todas las peticiones (BD)
- ✅ Maneja CORS
- ✅ Implementa circuit breaker

**El Gateway no modifica datos, solo enruta y controla.**

---

### INSTRUCCIONES PARA EL APIGATEWAY

#### 1. CONFIGURACIÓN BÁSICA

**Puerto**: 8081  
**Backend Target**: http://localhost:8080  
**BD**: PostgreSQL (compartida con Backend)  
**Cache**: Redis

```properties
# Gateway (application.properties)
server.port=8081
backend.url=http://localhost:8080
backend.timeout=30000

# JWT
jwt.secret=tu_secret_key_aqui
jwt.expiration=3600000

# Rate Limiting
ratelimit.user.requests.per.minute=100
ratelimit.global.requests.per.minute=1000

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Base de datos (auditoría)
spring.datasource.url=jdbc:postgresql://localhost:5432/medigo
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

---

#### 2. RUTAS QUE NO REQUIEREN JWT

El Gateway debe permitir estas rutas SIN validar JWT:

```java
public static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
    "/api/auth/login",
    "/api/auth/register",
    "/api/medications/search",
    "/api/medications/branch/*/stock",
    "/api/medications/branch/*/medications",
    "/api/medications/branches"
);
```

**Lógica**: Si la ruta está en PUBLIC_ENDPOINTS → forward directo al Backend

---

#### 3. RUTAS QUE REQUIEREN JWT

Todas las demás rutas bajo `/api/*` que NO estén en PUBLIC_ENDPOINTS requieren JWT válido.

**Validación JWT**:
1. Extraer Header: `Authorization: Bearer <token>`
2. Validar firma con `jwt.secret`
3. Validar expiración
4. Extraer `userId` y `roles` del JWT
5. Si inválido → retornar 401 Unauthorized

---

#### 4. RATE LIMITING (Redis)

**Por Usuario**:
- Máximo 100 peticiones/minuto
- Key en Redis: `user:{userId}:requests:{minuto}`
- Incrementar contador cada petición
- Si supera 100 → retornar 429 Too Many Requests

**Global**:
- Máximo 1000 peticiones/minuto
- Key en Redis: `global:requests:{minuto}`
- Si supera 1000 → retornar 429

**Expiración**: 60 segundos (auto-reset por minuto)

---

#### 5. AUDITORÍA (BD)

Crear tabla (si no existe):

```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    client_ip VARCHAR(45),
    method VARCHAR(10),
    endpoint VARCHAR(255),
    status_code INT,
    request_time TIMESTAMP,
    response_time TIMESTAMP,
    duration_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at)
);
```

**Loguear**:
- user_id (del JWT, null si es público)
- client_ip (del request)
- method (GET, POST, PUT, DELETE)
- endpoint (/api/orders/confirm)
- status_code (200, 401, 404, 500, etc.)
- duration_ms (tiempo de respuesta)

---

#### 6. FORWARDING AL BACKEND

**Reglas**:
- Método HTTP idéntico (GET, POST, PUT, DELETE)
- Headers idénticos (excepto JWT validation internal)
- Query parameters idénticos
- Body idéntico
- Retornar respuesta exacta del Backend (sin transformación)

```java
public ResponseEntity<?> forward(HttpServletRequest request) {
    String backendUrl = "http://localhost:8080" + request.getRequestURI();
    
    // Forwarding con RestTemplate
    // Mantener: método, headers, params, body
    // Retornar respuesta exacta del Backend
}
```

---

#### 7. CIRCUIT BREAKER

Si el Backend está down:
- Después de 5 errores consecutivos → abrir circuito
- Retornar 503 Service Unavailable
- Reintenta cerrar cada 30 segundos

Usar: Resilience4j

---

#### 8. ENDPOINTS QUE EL GATEWAY DEBE EXPONER

El Gateway expone EXACTAMENTE los mismos endpoints que el Backend. La diferencia es que el Gateway:
- Valida JWT primero
- Loguea la petición
- Aplica rate limiting
- Luego forwarda al Backend

**Lista completa de endpoints a exponer** (same como Backend):

```
PÚBLICOS:
POST   /api/auth/login
POST   /api/auth/register
GET    /api/medications/search?name=X
GET    /api/medications/branch/{branchId}/stock
GET    /api/medications/branch/{branchId}/medications
GET    /api/medications/branches

PROTEGIDOS:
GET    /api/auth/me
GET    /api/auth/{id}
GET    /api/auth/email/{email}
POST   /api/medications
PUT    /api/medications/{id}/stock
GET    /api/medications/{id}
POST   /api/orders/cart/add
GET    /api/orders/cart
DELETE /api/orders/cart/{cartId}/{medicationId}
POST   /api/orders/confirm
GET    /api/orders/{orderId}
GET    /api/orders/affiliate/{affiliateId}
PUT    /api/logistics/deliveries/{id}/complete
GET    /api/logistics/deliveries/active
GET    /api/logistics/deliveries/{id}
PUT    /api/logistics/deliveries/{id}/location
POST   /api/auctions
PUT    /api/auctions/{id}
GET    /api/auctions/{id}
GET    /api/auctions/active
GET    /api/auctions/{id}/bids
POST   /api/auctions/{id}/join
POST   /api/auctions/{id}/bids
GET    /api/auctions/{id}/winner
```

---

### FLUJO DE UNA PETICIÓN EN EL GATEWAY

```
1. Cliente envía: POST /api/orders/confirm + JWT
   ↓
2. Gateway recibe petición
   ↓
3. ¿Es petición pública (/api/auth/login)?
   ├─ SÍ → Ir a paso 6 (forward sin validar JWT)
   └─ NO → Ir a paso 4
   ↓
4. Validar JWT en header Authorization
   ├─ NO existe header → 401 Unauthorized
   ├─ Token inválido/expirado → 401 Unauthorized
   └─ SÍ válido → Extraer userId y roles, ir a paso 5
   ↓
5. Verificar rate limiting (Redis)
   ├─ Cliente superó 100 req/min → 429 Too Many Requests
   ├─ Global superó 1000 req/min → 429 Too Many Requests
   └─ OK → User puede proceder
   ↓
6. Forward al Backend (:8080)
   ├─ Mantener: método, headers, query params, body
   ├─ Backend procesa
   └─ Backend retorna respuesta
   ↓
7. Loguear en audit_logs
   ├─ user_id, client_ip, method, endpoint, status_code, duration_ms
   ↓
8. Retornar respuesta al cliente
   └─ EXACTAMENTE igual a respuesta del Backend
```

---

### TESTING DEL APIGATEWAY

**Test 1: Endpoint público sin JWT**
```
POST /api/auth/login
Body: {"email": "user@test.com", "password": "pass"}
Esperado: 200 OK (respuesta del Backend)
```

**Test 2: Endpoint protegido sin JWT**
```
POST /api/orders/confirm
Body: {...}
Esperado: 401 Unauthorized
```

**Test 3: Endpoint protegido con JWT válido**
```
POST /api/orders/confirm
Header: Authorization: Bearer <JWT válido>
Body: {...}
Esperado: Respuesta del Backend (200, 400, etc.)
```

**Test 4: JWT expirado**
```
GET /api/auth/me
Header: Authorization: Bearer <JWT expirado>
Esperado: 401 Unauthorized
```

**Test 5: Rate limiting**
```
Hacer 100 GET /api/orders/cart
→ Respuestas OK
Hacer 101 GET /api/orders/cart
→ 429 Too Many Requests
```

**Test 6: Auditoría**
```
Hacer POST /api/orders/confirm
→ Verificar que se registró en tabla audit_logs
→ user_id, client_ip, method, endpoint, status_code, duration_ms
```

---

### CAMBIOS RECIENTES EN BD DEL BACKEND

**Stock por sucursal** (tabla modificada):
```sql
-- ANTERIOR: stock en medications
-- ACTUAL: tabla branch_stock separada
CREATE TABLE branch_stock (
    id BIGINT PRIMARY KEY,
    medication_id BIGINT REFERENCES medications(id),
    branch_id BIGINT REFERENCES branches(id),
    quantity INT,
    UNIQUE(medication_id, branch_id)
);
```

**Impacto en Gateway**: Ninguno. Gateway solo forwarda peticiones tal cual son. El Backend sabe cómo manejar branch_stock.

---

### TABLA AUDIT_LOGS (NUEVA)

```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    client_ip VARCHAR(45),
    method VARCHAR(10),
    endpoint VARCHAR(255),
    status_code INT,
    request_time TIMESTAMP,
    response_time TIMESTAMP,
    duration_ms INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    KEY idx_endpoint (endpoint),
    KEY idx_status_code (status_code)
);
```

El Gateway es responsabilidad de poblar esta tabla.

---

### CHECKLIST DE IMPLEMENTACIÓN

- [ ] Crear proyecto Spring Boot en :8081
- [ ] Configurar RestTemplate + Interceptores
- [ ] Implementar JwtValidationInterceptor
- [ ] Implementar RateLimitingInterceptor (Redis)
- [ ] Implementar AuditLoggingInterceptor
- [ ] Crear tabla audit_logs
- [ ] Implementar forwards para todos los endpoints
- [ ] Test JWT validation
- [ ] Test Rate limiting
- [ ] Test Auditoría
- [ ] Test Proxy forwarding (idéntico al Backend)
- [ ] Test Circuit breaker
- [ ] Configurar application-prod.properties
- [ ] Deploy

---

## ✅ HISTORIAS DE USUARIO COMPLETADAS

### HU-01: Login de Usuario ✅
- JWT generado correctamente
- Credenciales validadas
- 139/139 tests PASSING

### HU-02: Autenticación ✅
- JWT válido en peticiones protegidas
- roles extraídos correctamente
- 139/139 tests PASSING

### HU-03: Búsqueda de Medicamentos ✅
- Búsqueda por nombre (LIKE, case-insensitive)
- Retorna lista de medicamentos
- 139/139 tests PASSING

### HU-04: Creación de Pedidos ✅
- Carrito (estado PENDING)
- Agregar/eliminar medicamentos
- Validación de stock
- 139/139 tests PASSING

### HU-05: Confirmación de Órdenes ✅
- Cambio de estado: PENDING → CONFIRMED
- Generación de número de orden
- Cálculo de total
- 139/139 tests PASSING

### HU-06: Asignación de Ruta de Entrega ✅
- Asignación automática de repartidor
- Estado: CONFIRMED → ASSIGNED
- 139/139 tests PASSING

### HU-07: Crear Medicamento (ADMIN) ✅
- Solo ADMIN puede crear
- Validación de datos
- 139/139 tests PASSING

### HU-08: Editar Stock (ADMIN) ✅
- Solo ADMIN puede editar
- Stock por sucursal (branchId)
- Validación: cantidad >= 0
- 139/139 tests PASSING

### HU-10: Confirmar Entrega ✅
- Repartidor presiona botón
- Estado: IN_ROUTE → DELIVERED
- Orden actualiza estado a ENTREGADO
- 139/139 tests PASSING

### HU-11: Entregas Activas ✅
- Repartidor ve sus entregas activas
- Filtrado por deliveryPersonId
- Estados: ASSIGNED, IN_ROUTE, PENDING_SHIPPING
- 139/139 tests PASSING

### HU-15: Crear Subasta (ADMIN) ✅
- Solo ADMIN
- Validación de fechas/precios
- Estados: SCHEDULED, ACTIVE, CLOSED

### HU-16: Editar Subasta (ADMIN) ✅
- Solo ADMIN
- Solo si está en SCHEDULED
- Modificar: precio, fechas, tipo cierre

### HU-17: Ver Detalle Subasta ✅
- Información completa
- Historial de pujas
- Precio actual

### HU-18: Unirse a Subasta ✅
- Cliente se registra para pujar
- Validación: subasta activa

### HU-19: Realizar Puja ✅
- Incremento de precio
- Validación: puja > puja anterior
- Redis SETNX para concurrencia

### HU-22: Consultar Ganador ✅
- Ganador de subasta cerrada
- Monto final

---

## 📞 RESUMEN PARA APIGATEWAY

**Instrucciones clave**:

1. El APIGATEWAY corre en :8081
2. Backend MediGo corre en :8080
3. GitAWY forwarda TODAS las peticiones `/api/*` al Backend
4. Valida JWT (excepto login/register)
5. Aplica rate limiting (Redis)
6. Audita peticiones (BD)
7. Retorna respuestas exactas del Backend (sin transformación)

**Endpoints**: 28+ endpoints totales (ver lista arriba)

**Autenticación**: JWT Bearer Token

**Rate Limit**: 100 req/min por usuario, 1000 req/min global

**Puertos**: Gateway :8081, Backend :8080

---

**¡Listo para implementar!** 🚀

