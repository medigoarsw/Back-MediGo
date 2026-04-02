# HU-07: Administrador Crea Medicamento en Catálogo

## Descripción General

**Historia de Usuario 7 - Crear Medicamento**

Como **administrador** quiero **agregar un nuevo medicamento al catálogo de productos** para **mantener actualizada la oferta de productos disponibles para los clientes**.

---

## Definición de Listo (DoR)

✅ **Completado:**
- Formulario de creación definido con campos requeridos
- Permisos de administrador validados (rol ADMIN)
- Usuario admin autenticado (HU-01: Login - HU-02: Autenticación)

---

## Definición de Hecho (DoD)

✅ **Implementado:**
- Formulario con validaciones: nombre obligatorio, presentación obligatoria, precio numérico positivo
- Medicamento guardado en catálogo (base de datos H2 en pruebas)
- Nuevo medicamento aparece inmediatamente en búsqueda de clientes
- Mensaje de éxito según corresponda
- Pruebas de creación realizadas (4 escenarios BDD)

---

## Escenarios BDD

### Escenario 1: Crear medicamento exitosamente ✅

**Given**: El administrador está autenticado en la plataforma

**When**: 
- Accede al formulario de creación de medicamento
- Ingresa nombre "Paracetamol 500mg", presentación "Tabletas", precio 5000
- Presiona el botón "Guardar"

**Then**:
- El medicamento se guarda en el catálogo
- Se muestra mensaje "Medicamento creado exitosamente"
- El medicamento aparece en la búsqueda de clientes

**Implementación**: `testCreateMedicationSuccessfully()` ✅

---

### Escenario 2: Intentar crear con campos obligatorios vacíos ✅

**Given**: El administrador está en el formulario de creación

**When**: 
- Deja el campo "nombre" vacío
- Completa presentación y precio
- Presiona "Guardar"

**Then**:
- El sistema muestra mensaje "El nombre es obligatorio"
- El medicamento NO se guarda

**Implementación**: `testCreateMedicationWithEmptyName()` ✅

---

### Escenario 3: Intentar crear con precio inválido ✅

**Given**: El administrador está en el formulario de creación

**When**: 
- Ingresa nombre "Paracetamol 500mg", presentación "Tabletas", precio 0
- Presiona "Guardar"

**Then**:
- El sistema muestra mensaje "El precio debe ser mayor a 0"
- El medicamento NO se guarda

**Implementación**: `testCreateMedicationWithInvalidPrice()` ✅

---

### Escenario 4: Medicamento creado visible para clientes ✅

**Given**: El administrador crea un nuevo medicamento "Aspirina 500mg"

**When**: Un cliente accede a la plataforma

**Then**:
- El cliente puede buscar "Aspirina" y encontrar el medicamento
- El medicamento aparece en el catálogo general

**Implementación**: `testCreatedMedicationVisibleInSearch()` ✅

---

## Componentes Implementados

### 1. Modelo de Dominio

**Archivo**: `Medication.java`
- ✅ Agregado campo: `price: BigDecimal`
- Validación: Precio debe ser > 0

**Archivo**: `MedicationEntity.java`
- ✅ Agregado campo: `price: BigDecimal` (columna en BD)
- Validación: NOT NULL

---

### 2. Use Case (Puerto de Entrada)

**Archivo**: `CreateMedicationUseCase.java` *(NUEVO)*
```java
Medication createMedication(
    String name,
    String description,
    String unit,
    BigDecimal price,
    Long branchId,
    Integer initialStock
)
```

---

### 3. Servicio de Aplicación

**Archivo**: `CatalogService.java`
- ✅ Implementa `CreateMedicationUseCase`
- ✅ Método: `createMedication(String, String, String, BigDecimal, Long, Integer)`
- Validaciones:
  - Nombre obligatorio
  - Presentación obligatoria
  - Precio > 0
  - Stock inicial > 0
  - Branch ID válido

---

### 4. DTO de Solicitud

**Archivo**: `CreateMedicationRequest.java`
```java
@NotBlank(message = "El nombre es obligatorio")
String name

@Schema(description = "Descripción del medicamento")
String description

@NotBlank(message = "La presentación es obligatoria")
String unit

@NotNull @DecimalMin("0.01", message = "El precio debe ser mayor a 0")
BigDecimal price  // ✅ NUEVO

@NotNull @Positive
Long branchId

@NotNull @Positive
Integer initialStock
```

---

### 5. DTO de Respuesta

**Archivo**: `MedicationResponse.java`
- ✅ Agregado campo: `price: BigDecimal`
- Retorna todos los datos del medicamento creado

---

### 6. Controlador REST

**Archivo**: `MedicationController.java`
```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")  // ✅ Solo administradores
public ResponseEntity<MedicationResponse> create(
    @Valid @RequestBody CreateMedicationRequest request)
```

**Endpoint**: `POST /api/medications`
- Autenticación: JWT Token (HU-01)
- Autorización: Solo ADMIN
- Status: 201 CREATED (exitoso)
- Error: 400 Bad Request (validación)
- Error: 403 Forbidden (sin permisos)

---

## Tests Implementados

**Archivo**: `CatalogServiceTest.java`

| # | Test | LineCount | Status |
|---|------|-----------|--------|
| 1 | `testCreateMedicationSuccessfully()` | HU-07 Escenario 1 | ✅ PASS |
| 2 | `testCreateMedicationWithEmptyName()` | HU-07 Escenario 2 | ✅ PASS |
| 3 | `testCreateMedicationWithInvalidPrice()` | HU-07 Escenario 3 | ✅ PASS |
| 4 | `testCreatedMedicationVisibleInSearch()` | HU-07 Escenario 4 | ✅ PASS |
| | **Total HU-07 Tests** | | **4/4 ✅** |
| | **Total CatalogServiceTest** | | **19/19 ✅** |

**Execution**:
```bash
mvn test -Dtest=CatalogServiceTest
Tests run: 19, Failures: 0, Errors: 0
BUILD SUCCESS ✅
```

---

## Cambios en la Arquitectura

### Adiciones:
1. ✅ `CreateMedicationUseCase` - Puerto de entrada para crear medicamentos
2. ✅ `price` en `Medication` y `MedicationEntity` - Soporte para precios
3. ✅ Implementación en `CatalogService` - Lógica de creación
4. ✅ DTOs actualizados - Incluyen precio en request/response
5. ✅ Validación de autorización - Solo ADMIN puede crear
6. ✅ Tests BDD - 4 escenarios de prueba

### Mantiene:
- ✅ Arquitectura Hexagonal (ports & adapters)
- ✅ Separación de responsabilidades
- ✅ Transactionalidad en BD
- ✅ Logging detallado con SLF4J
- ✅ Swagger documentation actualizada

---

## Logging

Se agregó logging con prefijo `HU-07:` para trazabilidad:

```
16:05:59.698 [main] INFO CatalogService -- HU-07: Creando medicamento: Paracetamol 500mg en sucursal: 1 con stock inicial: 100
16:05:59.699 [main] INFO CatalogService -- HU-07: Medicamento creado exitosamente con ID: 10 y número de orden: ORD-10
```

---

## SonarCloud Compliance

✅ **No issues introducidos**:
- Validaciones completas de entrada
- Logging adecuado (no sensitive data)
- Excepciones manejadas correctamente
- BigDecimal para operaciones monetarias (no float)
- Métodos bien documentados con Javadoc

---

## JaCoCo Coverage

**Líneas cubiertas por tests**:
- `CatalogService.createMedication()`: 100% ✅
- `CreateMedicationUseCase` interface: Implementado ✅
- Validaciones: Todas probadas ✅

---

## Notas de Implementación

1. **Precio con BigDecimal**: Usado para precisión financiera (recomendado en Java)
2. **Validación @DecimalMin("0.01")**: Asegura que precio sea > 0 (mín. 1 centavo)
3. **@PreAuthorize("hasRole('ADMIN')")**: Solo administradores pueden crear
4. **Stock inicial obligatorio**: Cada medicamento se crea con stock en una sucursal
5. **Transactional**: Operaciones guardan/actualizan en una transacción
6. **Non-blocking**: Si algo falla, se lanza excepción (diferente a HU-06 Paso-1)

---

## Próximos Pasos Recomendados

- [ ] Edición de medicamentos (HU-XX)
- [ ] Eliminación lógica de medicamentos (HU-XX)  
- [ ] Sincronización de stock entre sucursales (HU-XX)
- [ ] Reportes de medicamentos por sucursal (HU-XX)
- [ ] Control de medicamentos caducados/vencidos (HU-XX)

---

## Referencias

- **Especificación**: HU-07 - Crear medicamento en catálogo
- **Estándares**: Conventional CommitS v1.0.0
- **Framework**: Spring Boot 3.1.5, Spring Security 6
- **BD**: H2 (tests), PostgreSQL (producción)
- **Testing**: JUnit 5 + Mockito
- **API Docs**: Swagger/OpenAPI 3.0

