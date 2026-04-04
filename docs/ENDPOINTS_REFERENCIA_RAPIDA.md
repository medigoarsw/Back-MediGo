# 📝 Referencia Rápida - Endpoints Carrito

## ⚡ Cheat Sheet

### 📦 POST `/api/orders/cart/add` - Agregar al Carrito

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":2}'
```

| Campo | Tipo | Min | Max | Requerido |
|-------|------|-----|-----|-----------|
| `affiliateId` | Long | 1 | ∞ | ✅ |
| `branchId` | Long | 1 | ∞ | ✅ |
| `medicationId` | Long | 1 | ∞ | ✅ |
| `quantity` | int | 1 | 100 | ✅ |

**Respuestas:**
- ✅ `201 CREATED` - Éxito
- ❌ `400 BAD REQUEST` - Validación fallida

---

### 🛒 GET `/api/orders/cart` - Obtener Carrito

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1"
```

| Parámetro | Tipo | Requerido |
|-----------|------|-----------|
| `affiliateId` | Long | ✅ |
| `branchId` | Long | ✅ |

**Respuestas:**
- ✅ `200 OK` - Carrito encontrado (puede estar vacío)
- ❌ `404 NOT FOUND` - Carrito no existe

---

## 🎯 Casos Comunes

### ✅ Éxito: Agregar Medicamento
```
Status: 201
Body: {
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [{"medicationId": 5, "quantity": 2, ...}],
  "totalPrice": 50.00,
  "message": "Medicamento agregado al carrito exitosamente"
}
```

### ✅ Éxito: Obtener Carrito
```
Status: 200
Body: {
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {"medicationId": 5, "quantity": 2, "unitPrice": 25.00, "subtotal": 50.00},
    {"medicationId": 7, "quantity": 1, "unitPrice": 15.50, "subtotal": 15.50}
  ],
  "totalPrice": 65.50
}
```

### ❌ Error: Cantidad <= 0
```
Status: 400
Body: {"message": "La cantidad debe ser mayor a 0"}
```

### ❌ Error: Stock Excedido
```
Status: 400
Body: {"message": "No hay suficiente stock disponible..."}
```

### ❌ Error: Carrito No Existe
```
Status: 404
Body: {"message": "Carrito no encontrado"}
```

---

## 💾 Estructura JSON

### Request: AddToCartRequest
```json
{
  "affiliateId": 1,      // ← ID cliente
  "branchId": 1,         // ← Sucursal
  "medicationId": 5,     // ← Medicamento
  "quantity": 2          // ← Cantidad
}
```

### Response: CartResponse
```json
{
  "cartId": 42,                    // ID carrito
  "affiliateId": 1,
  "branchId": 1,
  "items": [                       // Array de items
    {
      "medicationId": 5,
      "quantity": 2,
      "unitPrice": 25.00,          // Precio por unidad
      "subtotal": 50.00            // qty × unitPrice
    }
  ],
  "totalPrice": 50.00,             // Suma de subtotales
  "message": "..."
}
```

---

## 🔴 Códigos de Error

| Código | Mensaje | Causa |
|--------|---------|-------|
| 400 | `La cantidad debe ser mayor a 0` | quantity ≤ 0 |
| 400 | `ID del cliente inválido` | affiliateId ≤ 0 |
| 400 | `ID de la sucursal inválido` | branchId ≤ 0 |
| 400 | `ID del medicamento inválido` | medicationId ≤ 0 |
| 400 | `No hay suficiente stock...` | quantity > 100 |
| 404 | `Carrito no encontrado` | Cliente sin carrito PENDING |
| 500 | Error interno | Excepción no manejada |

---

## 📊 Ejemplos Quick Copy-Paste

### Ejemplo 1: Agregar Medicamento
```json
POST http://localhost:8080/api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```

### Ejemplo 2: Obtener Carrito
```
GET http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1
```

### Ejemplo 3: Múltiples Medicamentos
```json
// Paso 1: Agregar medicamento 1
POST http://localhost:8080/api/orders/cart/add
{"affiliateId": 1, "branchId": 1, "medicationId": 5, "quantity": 2}

// Paso 2: Agregar medicamento 2
POST http://localhost:8080/api/orders/cart/add
{"affiliateId": 1, "branchId": 1, "medicationId": 7, "quantity": 1}

// Paso 3: Ver carrito
GET http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1
```

---

## ✅ Test Checklist

```
[ ] POST /cart/add con datos válidos → 201
[ ] GET /cart con cliente existente → 200
[ ] POST /cart/add cantidad = 0 → 400
[ ] POST /cart/add mismo medicamento 2x → incrementa qty
[ ] GET /cart cliente inexistente → 404
[ ] POST /cart/add cantidad > 100 → 400
```

---

## 🔗 Enlaces Útiles

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI Docs** | http://localhost:8080/v3/api-docs |
| **H2 Database** | http://localhost:8080/h2-console |
| **Actuator** | http://localhost:8080/actuator |

---

## 📱 Headers Requeridos

```
Content-Type: application/json
```

---

## ⏱️ Timeouts Recomendados

- **POST /cart/add**: 5 segundos
- **GET /cart**: 3 segundos

---

## 📖 Documentación Completa

- **API completa**: `docs/API_CARRITO_DOCUMENTACION.md`
- **Guía testing**: `docs/GUIA_TESTING_CARRITO.md`
- **Historia usuario**: `docs/HISTORIA_USUARIO_CARRITO.md`

---

## 🎓 Reglas de Negocio

1. ✅ Stock disponible debe ser > 0
2. ✅ No se duplican medicamentos
3. ✅ Se incrementa qty si existe
4. ✅ Total se calcula automáticamente
5. ✅ Límite máximo: 100 unidades por medicamento
6. ✅ Carrito es PENDING hasta confirmar

---

## 🧪 Test Rápido en Swagger

1. Abre http://localhost:8080/swagger-ui.html
2. Busca "Orders"
3. POST /api/orders/cart/add → Try it out
4. Ingresa: `{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":2}`
5. Execute
6. ✅ Deberías ver 201 CREATED

---

## 💡 Tips

- Usa `affiliateId=1, branchId=1, medicationId=5-15` con datos de prueba
- Los precios son fijos: $25.00 por defecto (MVP)
- Para tests: Python/cURL es más rápido
- Para debugging: Postman es más visual
- El carrito NO se elimina automáticamente

---

## 🚀 Flujo Completo en 60 Segundos

```
1. POST /cart/add (medicationId=5, qty=2)
   ↓ 201 Created
2. POST /cart/add (medicationId=7, qty=1)
   ↓ 201 Created
3. GET /cart
   ↓ 200 OK - Ves carrito con 2 items
4. POST /cart/add (medicationId=5, qty=1)
   ↓ 201 Created - medicationId=5 ahora tiene qty=3
5. GET /cart
   ↓ 200 OK - Carrito actualizado: totalPrice = $90.50
```

---

## 📋 Template para Bug Reports

```
Endpoint: POST /api/orders/cart/add
Status Code: [400/404/500]
Request: {"affiliateId": X, "branchId": Y, "medicationId": Z, "quantity": N}
Response: [paste error message]
Expected: [describe expected behavior]
```
