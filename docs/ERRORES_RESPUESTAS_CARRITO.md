# 🔴 Guía de Errores - API Carrito

## POST `/api/orders/cart/add`

### ✅ 201 - Éxito: Medicamento Agregado

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 2
  }'
```

**Respuesta:**
```json
{
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {
      "medicationId": 5,
      "quantity": 2,
      "unitPrice": 25.00,
      "subtotal": 50.00
    }
  ],
  "totalPrice": 50.00,
  "message": "Medicamento agregado al carrito exitosamente"
}
```

---

### ❌ 400 - Error: Cantidad Inválida (≤ 0)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 0
  }'
```

**Respuesta:**
```json
{
  "message": "La cantidad debe ser mayor a 0"
}
```

---

### ❌ 400 - Error: Cantidad Negativa

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": -5
  }'
```

**Respuesta:**
```json
{
  "message": "La cantidad debe ser mayor a 0"
}
```

---

### ❌ 400 - Error: Stock Excedido (> 100)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 101
  }'
```

**Respuesta:**
```json
{
  "message": "No hay suficiente stock disponible. Máximo permitido es 100 unidades"
}
```

---

### ❌ 400 - Error: ID Cliente Inválido (≤ 0)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 0,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 2
  }'
```

**Respuesta:**
```json
{
  "message": "ID del cliente inválido"
}
```

---

### ❌ 400 - Error: ID Sucursal Inválido (≤ 0)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": -1,
    "medicationId": 5,
    "quantity": 2
  }'
```

**Respuesta:**
```json
{
  "message": "ID de la sucursal inválido"
}
```

---

### ❌ 400 - Error: ID Medicamento Inválido (≤ 0)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 0,
    "quantity": 2
  }'
```

**Respuesta:**
```json
{
  "message": "ID del medicamento inválido"
}
```

---

### ❌ 500 - Error Interno

```bash
# Esto ocurre por excepciones no manejadas del servidor
```

**Respuesta:**
```json
{
  "message": "Error interno del servidor. Por favor intenta nuevamente"
}
```

---

## GET `/api/orders/cart`

### ✅ 200 - Éxito: Carrito Obtenido Con Items

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1"
```

**Respuesta:**
```json
{
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {
      "medicationId": 5,
      "quantity": 2,
      "unitPrice": 25.00,
      "subtotal": 50.00
    },
    {
      "medicationId": 7,
      "quantity": 1,
      "unitPrice": 15.50,
      "subtotal": 15.50
    }
  ],
  "totalPrice": 65.50,
  "message": "Carrito obtenido exitosamente"
}
```

---

### ✅ 200 - Éxito: Carrito Vacío

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1"
```

**Respuesta:**
```json
{
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [],
  "totalPrice": 0.00,
  "message": "Carrito obtenido exitosamente"
}
```

---

### ❌ 400 - Error: affiliateId Inválido (≤ 0)

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=0&branchId=1"
```

**Respuesta:**
```json
{
  "message": "affiliateId debe ser mayor a 0"
}
```

---

### ❌ 400 - Error: branchId Inválido (≤ 0)

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=-5"
```

**Respuesta:**
```json
{
  "message": "branchId debe ser mayor a 0"
}
```

---

### ❌ 400 - Error: Parámetro Faltante (affiliateId)

```bash
curl -X GET "http://localhost:8080/api/orders/cart?branchId=1"
```

**Respuesta:**
```json
{
  "message": "Parámetro requerido 'affiliateId' no presente"
}
```

---

### ❌ 400 - Error: Parámetro Faltante (branchId)

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1"
```

**Respuesta:**
```json
{
  "message": "Parámetro requerido 'branchId' no presente"
}
```

---

### ❌ 404 - Error: Carrito No Encontrado

```bash
# Cuando el cliente no tiene carrito pendiente
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=999&branchId=999"
```

**Respuesta:**
```json
{
  "message": "Carrito no encontrado para el cliente 999 en la sucursal 999"
}
```

---

### ❌ 500 - Error Interno

```bash
# Esto ocurre por excepciones no manejadas del servidor
```

**Respuesta:**
```json
{
  "message": "Error interno del servidor. Por favor intenta nuevamente"
}
```

---

## 📊 Tabla Resumen de Códigos HTTP

| Código | Evento | Endpoint | Causa |
|--------|--------|----------|-------|
| **201** | ✅ Éxito | POST /cart/add | Medicamento agregado |
| **200** | ✅ Éxito | GET /cart | Carrito obtenido |
| **400** | ❌ Bad Request | POST /cart/add | Validación fallida |
| **400** | ❌ Bad Request | GET /cart | Parámetros inválidos |
| **404** | ❌ Not Found | GET /cart | Carrito no existe |
| **500** | ❌ Internal Error | Ambos | Error del servidor |

---

## 🔍 Todos los Mensajes de Error

### Para POST `/api/orders/cart/add` (Código 400)

| Mensaje | Causa | Solución |
|---------|-------|----------|
| `La cantidad debe ser mayor a 0` | `quantity <= 0` | Usar `quantity >= 1` |
| `No hay suficiente stock disponible. Máximo permitido es 100 unidades` | `quantity > 100` | Usar `quantity <= 100` |
| `ID del cliente inválido` | `affiliateId <= 0` | Usar ID válido > 0 |
| `ID de la sucursal inválido` | `branchId <= 0` | Usar ID válido > 0 |
| `ID del medicamento inválido` | `medicationId <= 0` | Usar ID válido > 0 |

### Para GET `/api/orders/cart` (Código 400)

| Mensaje | Causa | Solución |
|---------|-------|----------|
| `affiliateId debe ser mayor a 0` | `affiliateId <= 0` | Usar ID válido > 0 |
| `branchId debe ser mayor a 0` | `branchId <= 0` | Usar ID válido > 0 |
| `Parámetro requerido 'affiliateId' no presente` | Falta parámetro | Incluir `?affiliateId=...` |
| `Parámetro requerido 'branchId' no presente` | Falta parámetro | Incluir `&branchId=...` |

### Para GET `/api/orders/cart` (Código 404)

| Mensaje | Causa | Solución |
|---------|-------|----------|
| `Carrito no encontrado para el cliente X en la sucursal Y` | No existe carrito PENDING | Agregar items con POST /cart/add primero |

---

## 📝 Test Script (Bash)

```bash
#!/bin/bash

echo "=== TEST 1: Agregar medicamento (201) ==="
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":2}' | jq

echo -e "\n=== TEST 2: Cantidad inválida (400) ==="
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":0}' | jq

echo -e "\n=== TEST 3: Stock excedido (400) ==="
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":150}' | jq

echo -e "\n=== TEST 4: Obtener carrito (200) ==="
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1" | jq

echo -e "\n=== TEST 5: Carrito no encontrado (404) ==="
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=999&branchId=999" | jq

echo -e "\n=== TEST 6: Parámetro faltante (400) ==="
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1" | jq
```

---

## 🧪 Verificación de Respuesta

Para verificar que recibiste la respuesta correcta:

```bash
# Ver solo el código de estado
curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/orders/cart/add \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":2}'

# Respuesta esperada: 201

# Ver respuesta con formato
curl -s http://localhost:8080/api/orders/cart/add \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"affiliateId":1,"branchId":1,"medicationId":5,"quantity":2}' | jq '.'
```

---

## 📱 Postman Collection

Ver: `ENDPOINTS_REFERENCIA_RAPIDA.md` para ejemplos en Postman

---

## 🔗 Swagger UI

Accede a la documentación interactiva en:
```
http://localhost:8080/swagger-ui.html
```

Busca la sección **"Orders"** → **"ordenaciones"** y prueba los endpoints desde la interfaz.
