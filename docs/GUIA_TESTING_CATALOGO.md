# 🧪 Guía de Testing - Módulo Catálogo/Inventario

## 🚀 Inicio Rápido

### 1. Compilar el código
```bash
cd "d:\ander\Documents\SEMESTRE 7\ARSW\PROYECTO OFICIAL\Back-MediGo"
mvn clean compile
```

### 2. Ejecutar las pruebas unitarias
```bash
mvn test
```

### 3. Ejecutar la aplicación
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

---

## 📖 Testing con Swagger UI

### Caso de Uso 1: Crear Medicamento

**Endpoint:** `POST /api/medications`

**Pasos en Swagger:**

1. Hacer clic en la sección **"Catalog"**
2. Expandir el endpoint `POST /api/medications`
3. Hacer clic en "Try it out"
4. Reemplazar el JSON con:

```json
{
  "name": "Paracetamol 500mg",
  "description": "Analgésico y antipirético para dolor leve a moderado",
  "unit": "tableta",
  "branchId": 1,
  "initialStock": 100
}
```

5. Hacer clic en "Execute"
6. **Resultado esperado**: Status 201 Created
7. **Cuerpo de respuesta**:
```json
{
  "id": 1,
  "name": "Paracetamol 500mg",
  "description": "Analgésico y antipirético para dolor leve a moderado",
  "unit": "tableta"
}
```

---

### Caso de Uso 2: Búsqueda de Medicamentos

**Endpoint:** `GET /api/medications/search`

**Pasos en Swagger:**

1. Hacer clic en `GET /api/medications/search`
2. Hacer clic en "Try it out"
3. En el parámetro `name`, ingresar: `paracetamol`
4. Hacer clic en "Execute"
5. **Resultado esperado**: Status 200 OK
6. **Cuerpo de respuesta**:
```json
[
  {
    "id": 1,
    "name": "Paracetamol 500mg",
    "description": "Analgésico y antipirético para dolor leve a moderado",
    "unit": "tableta"
  }
]
```

**Pruebas adicionales:**
- Búsqueda con texto vacío → Error 400
- Búsqueda con texto que no existe → Array vacío []
- Búsqueda insensible a mayúsculas: `PARACETAMOL`, `Paracetamol`, `paracetamol` → Mismo resultado

---

### Caso de Uso 3: Obtener Stock de Sucursal

**Endpoint:** `GET /api/medications/branch/{branchId}/stock`

**Pasos en Swagger:**

1. Hacer clic en `GET /api/medications/branch/{branchId}/stock`
2. Hacer clic en "Try it out"
3. En el parámetro `branchId`, ingresar: `1`
4. Hacer clic en "Execute"
5. **Resultado esperado**: Status 200 OK
6. **Cuerpo de respuesta**:
```json
[
  {
    "medicationId": 1,
    "medicationName": "Paracetamol 500mg",
    "branchId": 1,
    "quantity": 100,
    "isAvailable": true,
    "unit": "tableta"
  }
]
```

---

### Caso de Uso 4: Actualizar Stock

**Endpoint:** `PUT /api/medications/{medicationId}/branch/{branchId}/stock`

**Pasos en Swagger:**

1. Hacer clic en `PUT /api/medications/{medicationId}/branch/{branchId}/stock`
2. Hacer clic en "Try it out"
3. Ingresar parámetros:
   - `medicationId`: `1`
   - `branchId`: `1`
4. En el cuerpo del request, ingresar:
```json
{
  "medicationId": 1,
  "quantity": 50
}
```
5. Hacer clic en "Execute"
6. **Resultado esperado**: Status 204 No Content
7. **Verificación**: Llamar a `GET /api/medications/branch/1/stock` nuevamente para confirmar que la cantidad es 50

---

## 🔴 Casos de Error

### Error 1: Crear medicamento sin nombre
```json
{
  "name": "",
  "unit": "tableta",
  "branchId": 1,
  "initialStock": 100
}
```
**Respuesta esperada**: Status 400
```json
{
  "status": 400,
  "message": "Validation failed",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/medications",
  "timestamp": "2026-04-01T08:32:34.123",
  "details": "{name=El nombre del medicamento es requerido}"
}
```

### Error 2: Actualizar stock con cantidad negativa
```json
{
  "medicationId": 1,
  "quantity": -10
}
```
**Respuesta esperada**: Status 400
```json
{
  "status": 400,
  "message": "La cantidad no puede ser negativa",
  "errorCode": "BUSINESS_ERROR",
  "path": "/api/medications/1/branch/1/stock",
  "timestamp": "2026-04-01T08:32:34.123"
}
```

### Error 3: Obtener stock con branchId inválido
**Endpoint:** `GET /api/medications/branch/-1/stock`

**Respuesta esperada**: Status 400
```json
{
  "status": 400,
  "message": "El ID de la sucursal debe ser válido",
  "errorCode": "BUSINESS_ERROR",
  "path": "/api/medications/branch/-1/stock",
  "timestamp": "2026-04-01T08:32:34.123"
}
```

### Error 4: Actualizar medicamento que no existe
**Endpoint:** `PUT /api/medications/999/branch/1/stock`

**Cuerpo:**
```json
{
  "medicationId": 999,
  "quantity": 50
}
```

**Respuesta esperada**: Status 404
```json
{
  "status": 404,
  "message": "Medicamento no encontrado con ID: 999",
  "errorCode": "RESOURCE_NOT_FOUND",
  "path": "/api/medications/999/branch/1/stock",
  "timestamp": "2026-04-01T08:32:34.123"
}
```

---

## 🧬 Pruebas Unitarias Automatizadas

### Ejecutar todas las pruebas
```bash
mvn test
```

### Ejecutar pruebas específicas
```bash
# Pruebas del servicio
mvn test -Dtest=CatalogServiceTest

# Pruebas del controlador
mvn test -Dtest=MedicationControllerTest

# Pruebas del repositorio
mvn test -Dtest=MedicationJpaRepositoryTest

# Pruebas del manejo de excepciones
mvn test -Dtest=GlobalExceptionHandlerTest
```

### Ver cobertura (JaCoCo)
```bash
mvn clean test jacoco:report
# Abrir: target/site/jacoco/index.html
```

---

## 📊 Flujo de Testing Completo

```
1. Crear Medicamento
   ↓
2. Buscar el medicamento creado
   ↓
3. Consultar stock de sucursal
   ↓
4. Actualizar el stock (aumentar)
   ↓
5. Verificar que el stock fue actualizado
   ↓
6. Actualizar el stock (disminuir a 0)
   ↓
7. Verificar que aparece como no disponible (quantity = 0)
```

### Commands para Testing Automatizado:

```bash
#!/bin/bash

# Compilar
mvn clean compile

# Test
mvn test

# Ejecutar app
mvn spring-boot:run -Dspring-boot.run.profiles=local &

# Esperar a que inicie
sleep 10

# Testing con curl (linux) o PowerShell (windows)

# 1. Crear medicamento
curl -X POST http://localhost:8080/api/medications \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ibuprofeno 200mg",
    "description": "Antiinflamatorio",
    "unit": "tableta",
    "branchId": 1,
    "initialStock": 50
  }'

# 2. Buscar
curl http://localhost:8080/api/medications/search?name=ibuprofeno

# 3. Get stock
curl http://localhost:8080/api/medications/branch/1/stock

# 4. Update stock
curl -X PUT http://localhost:8080/api/medications/2/branch/1/stock \
  -H "Content-Type: application/json" \
  -d '{"medicationId": 2, "quantity": 25}'
```

---

## ✅ Checklist de Validación

- [ ] Compilación sin errores (`mvn clean compile`)
- [ ] Tests unitarios pasan (`mvn test`)
- [ ] Aplicación inicia correctamente
- [ ] Swagger UI accesible en `/swagger-ui.html`
- [ ] Todos los 4 endpoints documentados en Swagger
- [ ] Crear medicamento → 201 Created
- [ ] Buscar medicamentos → 200 OK con datos
- [ ] Obtener stock → 200 OK con cantidad
- [ ] Actualizar stock → 204 No Content
- [ ] Errores retornan códigos HTTP correctos
- [ ] Mensajes de error son descriptivos
- [ ] Validaciones funcionan (ej: nombre vacío)
- [ ] Transacciones funcionan correctamente
- [ ] Logs aparecen en consola

---

## 🐛 Troubleshooting

### La aplicación no inicia
**Causa**: Problemas con BD o dependencias
**Solución**:
```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Swagger no está disponible
**Causa**: OpenAPI no está configurado
**Verificación**: Verificar que `OpenApiConfiguration.java` existe en `shared/infrastructure/config/`

### Tests fallan
**Causa**: Conflictos de MockBeans
**Solución**:
```bash
mvn clean test -Dtest=CatalogServiceTest -DreuseForks=false
```

### Error de Validación en DTOs
**Verificar**: Que todos los DTOs tienen anotaciones `@Valid` en el endpoint
**Ejemplo**:
```java
@PostMapping
public ResponseEntity<MedicationResponse> create(
    @Valid @RequestBody CreateMedicationRequest request) {
    // ...
}
```

---

## 📚 Recursos

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Jakarta Validation](https://jakarta.ee/specifications/bean-validation/)
- [OpenAPI 3.0](https://spec.openapis.org/oas/v3.0.3)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

**Versión**: 1.0.0  
**Última actualización**: Abril 2026
