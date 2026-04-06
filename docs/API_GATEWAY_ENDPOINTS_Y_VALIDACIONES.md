# MediGo Backend - Endpoints, Body, Responses y Validaciones de Roles para API Gateway

Fecha: 2026-04-03
Origen: codigo fuente actual del backend (controllers + DTOs + SecurityConfig)

## 1) Objetivo
Este documento sirve como contrato tecnico para configurar validaciones en API Gateway:
- Rutas y metodos HTTP existentes
- Body esperado por endpoint
- Responses esperadas (exito/error)
- Reglas de autorizacion por rol para implementar en Gateway

## 2) Roles canonicos del sistema
Roles en dominio backend:
- ADMIN
- AFFILIATE (cliente/paciente)
- DELIVERY (repartidor)

Nota: en algunos textos historicos aparece USUARIO/REPARTIDOR. Para Gateway, usar siempre:
- USUARIO -> AFFILIATE
- REPARTIDOR -> DELIVERY

## 3) Estado de seguridad actual del backend
Existe un toggle:
- Propiedad: app.security.open-all-endpoints
- true: todo permitAll (sin auth)
- false: seguridad activa (JWT + reglas)

Reglas activas cuando open-all=false:
- Publico: /api/auth/**, /swagger-ui/**, /v3/api-docs/**, /swagger-ui.html, /ws/**, /actuator/**
- Subastas:
  - POST /api/auctions -> ADMIN
  - PUT /api/auctions/** -> ADMIN
  - GET /api/auctions/** -> ADMIN o AFFILIATE
  - POST /api/auctions/** -> ADMIN o AFFILIATE
- Todo lo demas: authenticated

## 4) Reglas recomendadas para API Gateway (roles)
Estas reglas son las recomendadas de negocio para que Gateway haga control fino por endpoint:

- Auth:
  - POST /api/auth/login -> PUBLIC
  - POST /api/auth/register -> PUBLIC
  - GET /api/auth/me -> AUTHENTICATED
  - GET /api/auth/{id} -> ADMIN
  - GET /api/auth/email/{email} -> ADMIN

- Catalogo:
  - GET /api/medications/search -> PUBLIC
  - GET /api/medications/branch/{branchId}/stock -> PUBLIC
  - GET /api/medications/branch/{branchId}/medications -> PUBLIC
  - GET /api/medications/branches -> PUBLIC
  - GET /api/medications/{medicationId}/availability/branch/{branchId} -> PUBLIC
  - GET /api/medications/{medicationId}/availability/branches -> PUBLIC
  - POST /api/medications -> ADMIN
  - PUT /api/medications/{medicationId}/branch/{branchId}/stock -> ADMIN

- Ordenes/carrito:
  - POST /api/orders/cart/add -> AFFILIATE
  - GET /api/orders/cart -> AFFILIATE
  - POST /api/orders -> AFFILIATE
  - POST /api/orders/{branchId}/confirm -> AFFILIATE

- Logistica:
  - PUT /api/logistics/deliveries/{id}/complete -> DELIVERY
  - GET /api/logistics/deliveries/active -> DELIVERY
  - GET /api/logistics/deliveries/{id} -> DELIVERY
  - PUT /api/logistics/deliveries/{id}/location -> DELIVERY
  - POST /api/logistics/deliveries/assign -> ADMIN

- Subastas:
  - POST /api/auctions -> ADMIN
  - PUT /api/auctions/{id} -> ADMIN
  - GET /api/auctions/{id} -> ADMIN, AFFILIATE
  - GET /api/auctions/active -> ADMIN, AFFILIATE
  - GET /api/auctions/{id}/bids -> ADMIN, AFFILIATE
  - GET /api/auctions/{id}/winner -> ADMIN, AFFILIATE
  - POST /api/auctions/{id}/join -> ADMIN, AFFILIATE
  - POST /api/auctions/{id}/bids -> ADMIN, AFFILIATE

## 5) Formato de error estandar (global)
Muchos endpoints pueden responder errores con este contrato:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/...",
  "timestamp": "2026-04-03T21:10:00",
  "details": "..."
}
```

Campos:
- status: number
- message: string
- errorCode: string
- path: string
- timestamp: datetime
- details: string (opcional)

---

## 6) Endpoints - Auth (/api/auth)

### POST /api/auth/login
Auth Gateway: PUBLIC

Body:
```json
{
  "email": "student@medigo.com",
  "password": "123"
}
```
Validaciones:
- email requerido y formato email
- password requerido, 1..255 chars

200 Response:
```json
{
  "access_token": "fake-jwt.1.AFFILIATE.1774978129358",
  "token_type": "Bearer",
  "user_id": 1,
  "username": "user",
  "email": "user@medigo.com",
  "role": "AFFILIATE",
  "expires_in": 3600
}
```
Errores: 401, 404, 500

### POST /api/auth/register
Auth Gateway: PUBLIC

Body:
```json
{
  "name": "Juan Perez",
  "email": "juan@medigo.com",
  "password": "Pass123!",
  "role": "AFFILIATE"
}
```
Validaciones:
- name requerido, 3..100 chars
- email requerido y valido
- password requerida
- role requerido
- backend solo permite AFFILIATE o DELIVERY

201 Response:
```json
{
  "id": 10,
  "name": "Juan Perez",
  "email": "juan@medigo.com",
  "role": "AFFILIATE",
  "createdAt": "2026-04-03T21:10:00",
  "message": "Usuario registrado exitosamente"
}
```
Errores: 400, 500

### GET /api/auth/me?user_id={id}
Auth Gateway: AUTHENTICATED

Body: none

200 Response:
```json
{
  "user_id": 2,
  "username": "user",
  "email": "user@medigo.com",
  "role": "AFFILIATE",
  "active": true
}
```
Errores: 404

### GET /api/auth/{id}
Auth Gateway: ADMIN

Body: none
Response: UserResponseDto (mismo shape de /me)
Errores: 404

### GET /api/auth/email/{email}
Auth Gateway: ADMIN

Body: none
Response: UserResponseDto (mismo shape de /me)
Errores: 404

---

## 7) Endpoints - Catalogo (/api/medications)

### GET /api/medications/search?name={texto}
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
[
  {
    "id": 1,
    "name": "Paracetamol 500mg",
    "description": "AnalgAsico",
    "unit": "tableta",
    "price": 5000.0
  }
]
```
Errores: 400

### GET /api/medications/branch/{branchId}/stock
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
[
  {
    "medicationId": 1,
    "medicationName": "Paracetamol 500mg",
    "branchId": 5,
    "quantity": 35,
    "isAvailable": true,
    "unit": "tableta"
  }
]
```
Errores: 400

### GET /api/medications/branch/{branchId}/medications
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
[
  {
    "medicationId": 1,
    "medicationName": "Ibuprofeno 400mg",
    "description": "Antiinflamatorio",
    "unit": "Caja x30",
    "quantity": 150
  }
]
```
Errores: 400, 404

### GET /api/medications/branches
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
[
  {
    "branchId": 1,
    "branchName": "Sucursal Centro",
    "address": "Calle 10 # 5-20",
    "latitude": 4.7216,
    "longitude": -74.04499,
    "medications": [
      {
        "medicationId": 1,
        "medicationName": "Ibuprofeno 400mg",
        "description": "Antiinflamatorio",
        "unit": "Caja x30",
        "quantity": 150
      }
    ]
  }
]
```

### POST /api/medications
Auth Gateway: ADMIN

Body:
```json
{
  "name": "Paracetamol 500mg",
  "description": "Analgesico y antipiretico",
  "unit": "tableta",
  "price": 5000.0,
  "branchId": 1,
  "initialStock": 100
}
```
Validaciones:
- name requerido
- unit requerida
- price requerido y > 0
- branchId requerido y > 0
- initialStock requerido y > 0

201 Response:
```json
{
  "id": 10,
  "name": "Paracetamol 500mg",
  "description": "Analgesico y antipiretico",
  "unit": "tableta",
  "price": 5000.0
}
```
Errores: 400, 403

### PUT /api/medications/{medicationId}/branch/{branchId}/stock
Auth Gateway: ADMIN

Body:
```json
{
  "medicationId": 1,
  "quantity": 50
}
```
Validaciones:
- medicationId requerido
- quantity requerida y >= 0

204 Response: sin body
Errores: 400, 403, 404

### GET /api/medications/{medicationId}/availability/branch/{branchId}
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
{
  "branchId": 1,
  "branchName": null,
  "address": null,
  "latitude": null,
  "longitude": null,
  "quantity": 5,
  "isAvailable": true,
  "availabilityStatus": "Disponible"
}
```
Errores: 400, 404

### GET /api/medications/{medicationId}/availability/branches
Auth Gateway: PUBLIC

Body: none

200 Response:
```json
{
  "medicationId": 5,
  "medicationName": "Paracetamol 500mg",
  "description": "Analgesico y antipiretico",
  "unit": "tableta",
  "availabilityByBranch": [
    {
      "branchId": 1,
      "branchName": null,
      "address": null,
      "latitude": null,
      "longitude": null,
      "quantity": 5,
      "isAvailable": true,
      "availabilityStatus": "Disponible"
    }
  ],
  "totalAvailable": 25,
  "branchesWithStock": 3
}
```
Errores: 400, 404

---

## 8) Endpoints - Ordenes/Carrito (/api/orders)

### POST /api/orders/cart/add
Auth Gateway: AFFILIATE

Body:
```json
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```
Validaciones sugeridas Gateway:
- affiliateId > 0
- branchId > 0
- medicationId > 0
- quantity entre 1 y 100

201 Response:
```json
{
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {
      "medicationId": 5,
      "quantity": 2,
      "unitPrice": 25.0,
      "subtotal": 50.0
    }
  ],
  "totalPrice": 50.0,
  "message": "Medicamento agregado al carrito exitosamente"
}
```
Errores: 400, 500

### GET /api/orders/cart?affiliateId={id}&branchId={id}
Auth Gateway: AFFILIATE

Body: none

200 Response: mismo shape de CartResponse anterior
Errores: 400, 404, 500

### POST /api/orders
Auth Gateway: AFFILIATE

Body:
```json
{
  "affiliateId": 1,
  "branchId": 1,
  "addressLat": 4.6452,
  "addressLng": -74.0505,
  "notes": "Dejar en porteria"
}
```
Validaciones sugeridas Gateway:
- affiliateId > 0
- branchId > 0

201 Response:
```json
{
  "id": 7,
  "affiliateId": 1,
  "branchId": 1,
  "status": "PENDING",
  "totalAmount": 0.0,
  "deliveryFee": 5.0,
  "items": [],
  "notes": "Dejar en porteria",
  "createdAt": "2026-04-03T14:00:00",
  "message": "Carrito creado exitosamente"
}
```
Errores: 400, 500

### POST /api/orders/{branchId}/confirm?affiliateId={id}
Auth Gateway: AFFILIATE

Body:
```json
{
  "street": "Calle 10",
  "streetNumber": "50-20",
  "city": "Bogota",
  "commune": "Centro",
  "latitude": 4.711,
  "longitude": -74.0721
}
```
Validaciones:
- street requerida
- streetNumber requerido
- city requerida
- commune requerida
- latitude/longitude opcionales

200 Response (Order):
```json
{
  "id": 100,
  "orderNumber": "ORD-2026-000123",
  "affiliateId": 1,
  "branchId": 1,
  "auctionId": null,
  "finalPrice": null,
  "totalPrice": 65500.0,
  "status": "CONFIRMED",
  "street": "Calle 10",
  "streetNumber": "50-20",
  "city": "Bogota",
  "commune": "Centro",
  "addressLat": 4.711,
  "addressLng": -74.0721,
  "createdAt": "2026-04-03T14:35:00",
  "items": [
    {
      "orderId": 100,
      "medicationId": 5,
      "quantity": 2,
      "unitPrice": 25000.0
    }
  ]
}
```
Errores: 400, 404, 500

---

## 9) Endpoints - Logistica (/api/logistics)

### PUT /api/logistics/deliveries/{id}/location
Auth Gateway: DELIVERY

Body actual backend: Object (no contrato definido aun)

Response actual:
- 200 OK sin body

Recomendacion Gateway:
- no validar estructura JSON estricta hasta que backend defina DTO

### PUT /api/logistics/deliveries/{id}/complete
Auth Gateway: DELIVERY

Body: none

200 Response:
```json
{
  "id": 1,
  "orderId": 100,
  "deliveryPersonId": 5,
  "status": "DELIVERED",
  "assignedAt": "2026-04-02T14:30:00"
}
```
Errores: 400, 404

### GET /api/logistics/deliveries/active?deliveryPersonId={id}
Auth Gateway: DELIVERY

Body: none

200 Response:
```json
[
  {
    "id": 1,
    "orderId": 100,
    "deliveryPersonId": 5,
    "status": "IN_ROUTE",
    "assignedAt": "2026-04-02T14:30:00"
  }
]
```
Errores: 400, 401

### GET /api/logistics/deliveries/{id}?deliveryPersonId={id}
Auth Gateway: DELIVERY

Body: none

200 Response: DeliveryResponse (mismo shape anterior)
Errores: 401, 403, 404

### POST /api/logistics/deliveries/assign
Auth Gateway: ADMIN

Body actual backend: Object (no contrato definido aun)

Response actual:
- 200 OK sin body

Recomendacion Gateway:
- no validar estructura JSON estricta hasta que backend defina DTO

---

## 10) Endpoints - Subastas (/api/auctions)

### POST /api/auctions
Auth Gateway: ADMIN

Body:
```json
{
  "medicationId": 1,
  "branchId": 1,
  "basePrice": 10000.0,
  "startTime": "2026-04-04T10:00:00",
  "endTime": "2026-04-04T12:00:00",
  "closureType": "FIXED_TIME",
  "maxPrice": 20000.0,
  "inactivityMinutes": 5
}
```
Validaciones:
- medicationId requerido
- branchId requerido
- basePrice requerido y > 0
- startTime requerido
- endTime requerido

201 Response (AuctionResponse):
```json
{
  "id": 10,
  "medicationId": 1,
  "medicationName": null,
  "medicationUnit": null,
  "branchId": 1,
  "basePrice": 10000.0,
  "maxPrice": 20000.0,
  "startTime": "2026-04-04T10:00:00",
  "endTime": "2026-04-04T12:00:00",
  "status": "SCHEDULED",
  "closureType": "FIXED_TIME",
  "winnerId": null,
  "winnerName": null,
  "remainingSeconds": null
}
```
Errores tipicos: 400, 404, 409

### PUT /api/auctions/{id}
Auth Gateway: ADMIN

Body:
```json
{
  "basePrice": 12000.0,
  "startTime": "2026-04-04T10:30:00",
  "endTime": "2026-04-04T12:30:00"
}
```
Validaciones:
- basePrice requerido y > 0
- startTime requerido
- endTime requerido

200 Response: AuctionResponse
Errores tipicos: 400, 404, 409

### GET /api/auctions/{id}
Auth Gateway: ADMIN, AFFILIATE

Body: none
200 Response: AuctionResponse enriquecido (incluye medicationName, medicationUnit, winnerName, remainingSeconds)
Errores tipicos: 404

### GET /api/auctions/active
Auth Gateway: ADMIN, AFFILIATE

Body: none
200 Response:
```json
[
  {
    "id": 10,
    "medicationId": 1,
    "medicationName": null,
    "medicationUnit": null,
    "branchId": 1,
    "basePrice": 10000.0,
    "maxPrice": 20000.0,
    "startTime": "2026-04-04T10:00:00",
    "endTime": "2026-04-04T12:00:00",
    "status": "ACTIVE",
    "closureType": "FIXED_TIME",
    "winnerId": null,
    "winnerName": null,
    "remainingSeconds": null
  }
]
```

### GET /api/auctions/{id}/bids
Auth Gateway: ADMIN, AFFILIATE

Body: none

200 Response:
```json
[
  {
    "id": 1,
    "auctionId": 10,
    "userId": 2,
    "userName": "user",
    "amount": 13000.0,
    "placedAt": "2026-04-04T10:45:00"
  }
]
```

### GET /api/auctions/{id}/winner
Auth Gateway: ADMIN, AFFILIATE

Body: none

200 Response:
```json
{
  "auctionId": 10,
  "winnerId": 2,
  "winnerName": "user",
  "winningAmount": 18000.0
}
```
Si aun no hay ganador: 204 No Content

### POST /api/auctions/{id}/join?userId={id}
Auth Gateway: ADMIN, AFFILIATE

Body: none

204 Response: sin body
Errores tipicos: 404, 409

### POST /api/auctions/{id}/bids
Auth Gateway: ADMIN, AFFILIATE

Body:
```json
{
  "userId": 2,
  "userName": "user",
  "amount": 15000.0
}
```
Validaciones:
- userId requerido
- userName requerido
- amount requerido y > 0

201 Response:
```json
{
  "id": 11,
  "auctionId": 10,
  "userId": 2,
  "userName": "user",
  "amount": 15000.0,
  "placedAt": "2026-04-04T10:50:00"
}
```
Errores tipicos: 400, 404, 409

---

## 11) Reglas de validacion sugeridas para Gateway (tecnicas)

- Validar Content-Type application/json en todos los POST/PUT con body
- Validar required fields antes de enrutar
- Validar tipos numericos y minimos (ej. amount > 0)
- Validar path/query params requeridos
- Validar JWT y rol por ruta segun seccion 4
- Inyectar X-User-Id y X-User-Role al backend para trazabilidad
- Para endpoints de AFFILIATE/DELIVERY, validar consistencia:
  - affiliateId/deliveryPersonId de request debe coincidir con claim del token

## 12) Observaciones importantes

- Hay 2 endpoints de logistica con body Object aun no tipado:
  - PUT /api/logistics/deliveries/{id}/location
  - POST /api/logistics/deliveries/assign
  Se recomienda definir DTOs en backend para validaciones estrictas en Gateway.

- Existe un metodo myOrders en OrderController sin anotacion de ruta, por lo tanto NO es endpoint activo.

- Este documento describe el estado actual del codigo. Si cambian DTOs o mappings, actualizar este archivo.

---

## 13) Prompt recomendado para el otro proyecto (API Gateway)

Usa este bloque tal cual como prompt inicial en el repo del API Gateway:

```text
Contexto:
- Este backend ya cambio y el contrato de endpoints/roles vigente es el del documento adjunto API_GATEWAY_ENDPOINTS_Y_VALIDACIONES.md.
- Yo ya habia implementado validaciones y reglas en el API Gateway anteriormente, pero varias quedaron desalineadas o incompletas.
- NO quiero rehacer todo desde cero: quiero corregir y alinear lo existente con el contrato actual del backend.

Objetivo:
- Auditar la implementacion actual del API Gateway.
- Detectar diferencias contra API_GATEWAY_ENDPOINTS_Y_VALIDACIONES.md.
- Aplicar cambios minimos y seguros para dejar rutas, roles y validaciones alineadas.

Instrucciones obligatorias:
1. Usa EXCLUSIVAMENTE API_GATEWAY_ENDPOINTS_Y_VALIDACIONES.md como fuente de verdad.
2. No inventes endpoints, campos ni roles.
3. Respeta roles canonicos: ADMIN, AFFILIATE, DELIVERY.
4. Marca como NO ACTIVO cualquier metodo del backend que no tenga anotacion de ruta.
5. Prioriza ajustes incrementales (diff pequeno) sobre refactor grande.

Entregables requeridos:
1. Matriz de brechas (AS-IS vs TO-BE)
  - endpoint
  - estado_actual_gateway
  - estado_esperado
  - accion_recomendada
2. Politicas finales por ruta
  - path
  - method
  - auth: PUBLIC | AUTHENTICATED | ROLE_BASED
  - allowedRoles
3. Validaciones por endpoint
  - path params requeridos
  - query params requeridos
  - body schema minimo (required, tipo, min/max)
4. Lista de excepciones
  - endpoints con contrato incompleto en backend (body Object)
  - decision temporal sugerida en gateway

Reglas de salida:
- Primero muestra la matriz de brechas priorizada por impacto (alto, medio, bajo).
- Luego muestra el plan de cambios en orden de ejecucion.
- Luego entrega la configuracion final propuesta.
- Si hay ambiguedad, no inventes: reporta en una seccion warnings.
```

### Bloque corto adicional para resaltar cambios de backend

Si quieres enfatizarlo aun mas, agrega al prompt del Gateway este mini bloque:

```text
Importante:
- Hubo cambios recientes en backend (endpoints, validaciones y reglas de acceso).
- Toma como version valida unicamente el documento adjunto.
- Cualquier configuracion previa en Gateway que contradiga ese documento debe actualizarse.
```

