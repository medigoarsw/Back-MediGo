# 🧪 Guía de Testing - API Carrito de Compras

## 📌 Inicio Rápido

Para probar los endpoints, primero asegúrate de que la aplicación está corriendo:

```bash
mvn spring-boot:run
```

Luego de que veas:
```
[INFO] Started MediGoApplication in X.XXX seconds (JVM running for Y.YYY)
```

Puedes acceder a **Swagger UI**:
```
http://localhost:8080/swagger-ui.html
```

---

## 🛠️ Testing con cURL

### 1. Agregar Medicamento al Carrito

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 2
  }' \
  -w "\nStatus: %{http_code}\n"
```

**Respuesta esperada:** `201 CREATED`

---

### 2. Obtener Carrito Actual

```bash
curl -X GET "http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1" \
  -H "Content-Type: application/json" \
  -w "\nStatus: %{http_code}\n"
```

**Respuesta esperada:** `200 OK`

---

### 3. Agregar el Mismo Medicamento (Incrementar Cantidad)

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 3
  }' \
  -w "\nStatus: %{http_code}\n"
```

**Resultado:** Quantity pasa de 2 a 5 (se incrementa, no se duplica)

---

### 4. Agregar Medicamento Diferente al Mismo Carrito

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 7,
    "quantity": 1
  }' \
  -w "\nStatus: %{http_code}\n"
```

**Resultado:** Ahora el carrito tiene 2 medicamentos diferentes

---

### 5. Error - Cantidad Inválida

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 0
  }' \
  -w "\nStatus: %{http_code}\n"
```

**Respuesta esperada:** `400 BAD REQUEST`
```json
{
  "message": "La cantidad debe ser mayor a 0"
}
```

---

### 6. Error - Stock Insuficiente

```bash
curl -X POST http://localhost:8080/api/orders/cart/add \
  -H "Content-Type: application/json" \
  -d '{
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 150
  }' \
  -w "\nStatus: %{http_code}\n"
```

**Respuesta esperada:** `400 BAD REQUEST`
```json
{
  "message": "No hay suficiente stock disponible. Stock máximo permitido: 100"
}
```

---

## 🧩 Testing con Postman

### Descargar Colección
1. Ve a: `http://localhost:8080/v3/api-docs`
2. Copia todo el JSON
3. En Postman: `File` → `Import` → `Paste Raw Text`

### O Crear Manualmente

#### Request 1: Add to Cart
```
POST http://localhost:8080/api/orders/cart/add

Headers:
Content-Type: application/json

Body (raw):
{
  "affiliateId": 1,
  "branchId": 1,
  "medicationId": 5,
  "quantity": 2
}
```

#### Request 2: Get Cart
```
GET http://localhost:8080/api/orders/cart?affiliateId=1&branchId=1

Headers:
Content-Type: application/json
```

---

## 🐍 Testing con Python

### Script de Prueba Completo

```python
import requests
import json

BASE_URL = "http://localhost:8080/api/orders"

def print_response(response, title):
    print(f"\n{'='*60}")
    print(f"📋 {title}")
    print(f"Status: {response.status_code}")
    print(f"{'='*60}")
    try:
        data = response.json()
        print(json.dumps(data, indent=2))
    except:
        print(response.text)

# Test 1: Agregar primer medicamento
print("\n🧪 Test 1: Agregar medicamento al carrito")
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 2
}
response = requests.post(f"{BASE_URL}/cart/add", json=payload)
print_response(response, "POST /cart/add (Medicamento 1)")

# Test 2: Obtener carrito
print("\n🧪 Test 2: Obtener carrito actual")
response = requests.get(f"{BASE_URL}/cart", params={
    "affiliateId": 1,
    "branchId": 1
})
print_response(response, "GET /cart")

# Test 3: Agregar el mismo medicamento (incrementar)
print("\n🧪 Test 3: Agregar mismo medicamento (cantidad)")
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 3
}
response = requests.post(f"{BASE_URL}/cart/add", json=payload)
print_response(response, "POST /cart/add (Medicamento 1 - Segunda vez)")

# Test 4: Agregar medicamento diferente
print("\n🧪 Test 4: Agregar medicamento diferente")
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 7,
    "quantity": 1
}
response = requests.post(f"{BASE_URL}/cart/add", json=payload)
print_response(response, "POST /cart/add (Medicamento 2)")

# Test 5: Obtener carrito con múltiples items
print("\n🧪 Test 5: Carrito con múltiples medicamentos")
response = requests.get(f"{BASE_URL}/cart", params={
    "affiliateId": 1,
    "branchId": 1
})
print_response(response, "GET /cart (Completo)")

# Test 6: Error - Cantidad inválida
print("\n🧪 Test 6: Error - Cantidad inválida")
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 0
}
response = requests.post(f"{BASE_URL}/cart/add", json=payload)
print_response(response, "POST /cart/add (Cantidad inválida - ESPERADO ERROR)")

# Test 7: Error - Stock excedido
print("\n🧪 Test 7: Error - Stock excedido")
payload = {
    "affiliateId": 1,
    "branchId": 1,
    "medicationId": 5,
    "quantity": 150
}
response = requests.post(f"{BASE_URL}/cart/add", json=payload)
print_response(response, "POST /cart/add (Stock excedido - ESPERADO ERROR)")

# Test 8: Carrito no existe
print("\n🧪 Test 8: Carrito no existe")
response = requests.get(f"{BASE_URL}/cart", params={
    "affiliateId": 999,
    "branchId": 999
})
print_response(response, "GET /cart (Cliente inexistente - ESPERADO ERROR)")

print("\n\n✅ Pruebas completadas")
```

**Ejecutar:**
```bash
python test_carrito.py
```

---

## 📱 Testing con JavaScript/Node.js

### Script de Prueba Completo

```javascript
const BASE_URL = "http://localhost:8080/api/orders";

async function printResponse(response, title) {
  console.log("\n" + "=".repeat(60));
  console.log(`📋 ${title}`);
  console.log("=".repeat(60));
  console.log(`Status: ${response.status}`);
  const data = await response.json();
  console.log(JSON.stringify(data, null, 2));
}

async function runTests() {
  
  // Test 1: Agregar medicamento
  console.log("\n🧪 Test 1: Agregar medicamento al carrito");
  let response = await fetch(`${BASE_URL}/cart/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      affiliateId: 1,
      branchId: 1,
      medicationId: 5,
      quantity: 2
    })
  });
  await printResponse(response, "POST /cart/add (Medicamento 1)");

  // Test 2: Obtener carrito
  console.log("\n🧪 Test 2: Obtener carrito actual");
  response = await fetch(`${BASE_URL}/cart?affiliateId=1&branchId=1`);
  await printResponse(response, "GET /cart");

  // Test 3: Incrementar cantidad
  console.log("\n🧪 Test 3: Incrementar cantidad");
  response = await fetch(`${BASE_URL}/cart/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      affiliateId: 1,
      branchId: 1,
      medicationId: 5,
      quantity: 3
    })
  });
  await printResponse(response, "POST /cart/add (Medicamento 1 - Segunda vez)");

  // Test 4: Medicamento diferente
  console.log("\n🧪 Test 4: Agregar medicamento diferente");
  response = await fetch(`${BASE_URL}/cart/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      affiliateId: 1,
      branchId: 1,
      medicationId: 7,
      quantity: 1
    })
  });
  await printResponse(response, "POST /cart/add (Medicamento 2)");

  // Test 5: Carrito completo
  console.log("\n🧪 Test 5: Carrito con múltiples medicamentos");
  response = await fetch(`${BASE_URL}/cart?affiliateId=1&branchId=1`);
  await printResponse(response, "GET /cart (Completo)");

  // Test 6: Error
  console.log("\n🧪 Test 6: Error - Cantidad inválida");
  response = await fetch(`${BASE_URL}/cart/add`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      affiliateId: 1,
      branchId: 1,
      medicationId: 5,
      quantity: 0
    })
  });
  await printResponse(response, "POST /cart/add (ESPERADO ERROR)");

  console.log("\n\n✅ Pruebas completadas");
}

runTests().catch(console.error);
```

**Ejecutar:**
```bash
node test_carrito.js
```

---

## 🔍 Testing Manual en Swagger UI

1. Abre `http://localhost:8080/swagger-ui.html`
2. Busca **"Orders"** en la lista de tags
3. Expande **"POST /api/orders/cart/add"**
4. Haz clic en **"Try it out"**
5. Completa el formulario con datos de prueba
6. Haz clic en **"Execute"**
7. Observa la respuesta

---

## 📊 Comparación de Herramientas

| Herramienta | Ventajas | Desventajas |
|-------------|----------|------------|
| **Swagger UI** | Visual, sin instalación | Solo GET/POST simples |
| **cURL** | Rápido, línea de comandos | Requiere formato correcto |
| **Postman** | Más funcionalidades, historial | Aplicación separada |
| **Python** | Automatizable, scripts | Requiere Python instalado |
| **JavaScript** | En navegador directo | Requiere Node.js |

---

## ✅ Checklist de Testing

- [ ] Agregar 1 medicamento correctamente
- [ ] Agregar el mismo medicamento → incrementa qty
- [ ] Agregar medicamento diferente → nuevo item
- [ ] Obtener carrito con múltiples items
- [ ] Error: cantidad = 0 → 400
- [ ] Error: medicationId inválido → 400
- [ ] Error: stock excedido → 400
- [ ] Carrito inexistente → 404
- [ ] Validar totalPrice se calcula correctamente
- [ ] Validar subtotal de cada item

---

## 🐛 Troubleshooting

### Error: "Connection refused"
```
❌ Error: connect ECONNREFUSED 127.0.0.1:8080
```
**Solución:** Asegúrate de que la aplicación está corriendo
```bash
mvn spring-boot:run
```

### Error: "Content-Type"
```
❌ 415 Unsupported Media Type
```
**Solución:** Agrega header `Content-Type: application/json`

### Error: "Invalid JSON"
```
❌ 400 Json parse error
```
**Solución:** Valida el JSON en `https://jsonlint.com/`

### Error: "Missing required field"
```
❌ 400 Json parse error: Missing required field 'quantity'
```
**Solución:** Asegúrate de incluir TODOS los campos requeridos

---

## 📈 Ejemplo Completo de Flujo

```
1. Cliente crea cuenta (affiliateId = 1) en sucursal (branchId = 1)

2. Cliente busca medicamentos en catálogo
   ✓ Paracetamol 500mg (medicationId = 5) - $25.00
   ✓ Ibuprofeno 400mg (medicationId = 7) - $15.50

3. Cliente agrega Paracetamol x2
   POST /cart/add { affiliateId: 1, branchId: 1, medicationId: 5, quantity: 2 }
   ✓ Response 201: { items: [medication 5 qty:2], totalPrice: $50.00 }

4. Cliente ve el carrito
   GET /cart?affiliateId=1&branchId=1
   ✓ Response 200: { items: [...], totalPrice: $50.00 }

5. Cliente agrega Ibuprofeno x1
   POST /cart/add { affiliateId: 1, branchId: 1, medicationId: 7, quantity: 1 }
   ✓ Response 201: { items: [med5 qty:2, med7 qty:1], totalPrice: $65.50 }

6. Cliente quiere más Paracetamol
   POST /cart/add { affiliateId: 1, branchId: 1, medicationId: 5, quantity: 1 }
   ✓ Response 201: { items: [med5 qty:3, med7 qty:1], totalPrice: $90.50 }

7. Cliente verifica carrito final
   GET /cart?affiliateId=1&branchId=1
   ✓ Response 200: { items: [med5 qty:3 subtotal:$75, med7 qty:1 subtotal:$15.50], totalPrice: $90.50 }

8. Cliente procede a checkout (próximo endpoint)
```

---

## 📞 Soporte

Si encuentras problemas, revisa:
1. `docs/API_CARRITO_DOCUMENTACION.md` - Documentación completa
2. `docs/HISTORIA_USUARIO_CARRITO.md` - Detalles técnicos
3. Logs en consola: `mvn spring-boot:run`
4. Swagger UI: `http://localhost:8080/swagger-ui.html`
