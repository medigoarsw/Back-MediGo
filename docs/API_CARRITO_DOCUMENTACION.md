# 📖 Documentación API - Carrito de Compras

## Base URL
```
http://localhost:8080/api/orders
```

---

## 🛒 Endpoints del Carrito

### 1. **POST** `/api/orders/cart/add`

#### Descripción
Agrega un medicamento al carrito de compras del cliente. Si el medicamento ya existe en el carrito, incrementa la cantidad.

#### Método HTTP
```http
POST /api/orders/cart/add
Content-Type: application/json
```

#### Request Body
```json
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```

**Parámetros:**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `affiliateId` | Long | ✅ | ID del cliente (debe ser > 0) |
| `branchId` | Long | ✅ | ID de la sucursal (debe ser > 0) |
| `medicationId` | Long | ✅ | ID del medicamento a agregar (debe ser > 0) |
| `quantity` | int | ✅ | Cantidad a agregar (debe ser > 0) |

---

#### ✅ Response 201 CREATED - Éxito

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
  "message": "Medicamento agregado al carrito exitosamente"
}
```

---

#### ❌ Response 400 BAD REQUEST

##### Caso 1: Cantidad debe ser > 0
```json
{
  "message": "La cantidad debe ser mayor a 0"
}
```

##### Caso 2: ID del cliente inválido
```json
{
  "message": "ID del cliente inválido"
}
```

##### Caso 3: ID de la sucursal inválido
```json
{
  "message": "ID de la sucursal inválido"
}
```

##### Caso 4: ID del medicamento inválido
```json
{
  "message": "ID del medicamento inválido"
}
```

##### Caso 5: Stock insuficiente
```json
{
  "message": "No hay suficiente stock disponible. Stock máximo permitido: 100"
}
```

##### Caso 6: Request body inválido
```json
{
  "message": "Json parse error: Missing required field 'quantity'"
}
```

---

#### 📋 Ejemplos de Llamadas

**cURL:**
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

**JavaScript/Fetch:**
```javascript
const response = await fetch('http://localhost:8080/api/orders/cart/add', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    affiliateId: 1,
    branchId: 1,
    medicationId: 5,
    quantity: 2
  })
});

const data = await response.json();
console.log(data);
```

**Python:**
```python
import requests
import json

url = "http://localhost:8080/api/orders/cart/add"
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 2
}

response = requests.post(url, json=payload)
print(response.json())
```

---

### 2. **GET** `/api/orders/cart`

#### Descripción
Obtiene el carrito actual del cliente. Si no existe un carrito activo, retorna error 404.

#### Método HTTP
```http
GET /api/orders/cart?affiliateId=1&branchId=1
```

#### Query Parameters
| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| `affiliateId` | Long | ✅ | ID del cliente (debe ser > 0) |
| `branchId` | Long | ✅ | ID de la sucursal (debe ser > 0) |

---

#### ✅ Response 200 OK - Carrito Encontrado

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
  "message": null
}
```

#### ✅ Response 200 OK - Carrito Vacío

```json
{
  "cartId": 42,
  "affiliateId": 1,
  "branchId": 1,
  "items": [],
  "totalPrice": 0.00,
  "message": null
}
```

---

#### ❌ Response 404 NOT FOUND

```json
{
  "message": "Carrito no encontrado"
}
```

#### ❌ Response 400 BAD REQUEST

##### Caso: Parámetro faltante
```
GET /api/orders/cart?affiliateId=1
(Falta branchId)
```

Response:
```json
{
  "message": "Required Long parameter 'branchId' is not present"
}
```

---

#### 📋 Ejemplos de Llamadas

**cURL:**
```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1"
```

**JavaScript/Fetch:**
```javascript
const affiliateId = 1;
const branchId = 1;

const response = await fetch(
  `http://localhost:8080/api/orders/cart?affiliateId=${affiliateId}&branchId=${branchId}`,
  {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    }
  }
);

const data = await response.json();
console.log(data);
```

**Python:**
```python
import requests

url = "http://localhost:8080/api/orders/cart"
params = {
    "affiliateId": 1,
    "branchId": 1
}

response = requests.get(url, params=params)
print(response.json())
```

---

## 📊 Estructura de Datos

### AddToCartRequest
```typescript
{
  affiliateId: number,      // ID del cliente
  branchId: number,         // ID de la sucursal
  medicationId: number,     // ID del medicamento
  quantity: number          // Cantidad a agregar
}
```

### CartResponse
```typescript
{
  cartId: number,           // ID del carrito (Order ID)
  affiliateId: number,      // ID del cliente
  branchId: number,         // ID de la sucursal
  items: CartItemResponse[],// Lista de items en el carrito
  totalPrice: number,       // Total del carrito
  message: string          // Mensaje de confirmación o null
}
```

### CartItemResponse
```typescript
{
  medicationId: number,     // ID del medicamento
  quantity: number,         // Cantidad en el carrito
  unitPrice: number,        // Precio unitario
  subtotal: number          // Total del item (quantity × unitPrice)
}
```

### ErrorMessage
```typescript
{
  message: string           // Descripción del error
}
```

---

## 🔍 Códigos HTTP

| Código | Significado | Endpoint |
|--------|-------------|----------|
| **201** | Creado/Actualizado | POST /cart/add |
| **200** | OK | GET /cart |
| **400** | Bad Request (validación) | POST /cart/add, GET /cart |
| **404** | No encontrado | GET /cart |
| **500** | Error interno del servidor | Cualquiera |

---

## 📋 Casos de Uso Detallados

### Caso 1: Agregar Primer Producto al Carrito
**Request:**
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 1
}
```

**Response (201):**
```json
{
  "cartId": 10,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {
      "medicationId": 5,
      "quantity": 1,
      "unitPrice": 25.00,
      "subtotal": 25.00
    }
  ],
  "totalPrice": 25.00,
  "message": "Medicamento agregado al carrito exitosamente"
}
```

---

### Caso 2: Agregar el Mismo Producto Dos Veces
**Primera llamada:**
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 1
}
```
**Response:** cantidad = 1, total = $25.00

**Segunda llamada (mismo medicamento):**
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```
**Response (201):**
```json
{
  "cartId": 10,
  "affiliateId": 1,
  "branchId": 1,
  "items": [
    {
      "medicationId": 5,
      "quantity": 3,        // ← Incrementada (1 + 2)
      "unitPrice": 25.00,
      "subtotal": 75.00
    }
  ],
  "totalPrice": 75.00,      // ← Total actualizado
  "message": "Medicamento agregado al carrito exitosamente"
}
```

---

### Caso 3: Múltiples Medicamentos en Carrito
**Primer medicamento:**
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```
**Response:** 2 × $25.00 = $50.00

**Segundo medicamento:**
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 7,
  "quantity": 1
}
```
**Response (201):**
```json
{
  "cartId": 10,
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
  "message": "Medicamento agregado al carrito exitosamente"
}
```

---

### Caso 4: Error - Stock Insuficiente
```json
POST /api/orders/cart/add
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 150        // Excede límite de 100
}
```
**Response (400):**
```json
{
  "message": "No hay suficiente stock disponible. Stock máximo permitido: 100"
}
```

---

### Caso 5: Obtener Carrito Existente
```
GET /api/orders/cart?affiliateId=1&branchId=1
```
**Response (200):**
```json
{
  "cartId": 10,
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
  "message": null
}
```

---

### Caso 6: Carrito No Existe
```
GET /api/orders/cart?affiliateId=999&branchId=999
```
**Response (404):**
```json
{
  "message": "Carrito no encontrado"
}
```

---

## 🔐 Validaciones

### En AddToCartRequest
✅ `affiliateId > 0` → Error 400
✅ `branchId > 0` → Error 400
✅ `medicationId > 0` → Error 400
✅ `quantity > 0` → Error 400

### En Stock
✅ Cantidad solicitada ≤ 100 → Error 400
✅ No hay duplicación de medicamentos

---

## 💡 Notas Importantes

1. **Automático:** Si el medicamento ya existe, incrementa la cantidad automáticamente
2. **No hay límite de medicamentos diferentes:** Puedes tener 2, 3 o más medicamentos diferentes
3. **Límite de cantidad por medicamento:** Máximo 100 unidades (MVP)
4. **Carrito temporal:** Un carrito PENDING existe solo hasta que se confirme la orden
5. **Precios unitarios:** Se capturan al agregar ($25.00 por defecto en MVP)

---

## 🧪 Testing en Postman

### Collection JSON
```json
{
  "info": {
    "name": "MediGo Shopping Cart API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Add to Cart",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"affiliateId\": 1,\n  \"branchId\": 1,\n  \"medicationId\": 5,\n  \"quantity\": 2\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/orders/cart/add",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "orders", "cart", "add"]
        }
      }
    },
    {
      "name": "Get Cart",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "orders", "cart"],
          "query": [
            {
              "key": "affiliateId",
              "value": "1"
            },
            {
              "key": "branchId",
              "value": "1"
            }
          ]
        }
      }
    }
  ]
}
```

---

## 📞 Soporte

Para más información, revisa:
- `docs/HISTORIA_USUARIO_CARRITO.md` - Detalles técnicos de implementación
- `src/main/java/edu/escuelaing/arsw/medigo/orders/` - Código fuente
- `http://localhost:8080/swagger-ui.html` - Swagger UI en vivo
