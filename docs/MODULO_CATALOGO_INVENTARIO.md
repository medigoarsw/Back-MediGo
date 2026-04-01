# 📦 Módulo de Inventario/Catálogo - MediGo

## Descripción

Este módulo implementa la gestión completa del catálogo de medicamentos y control de inventario por sucursal en la plataforma MediGo. Utiliza una arquitectura **hexagonal (puertos y adaptadores)** con **Spring Boot 3.1.5** y **PostgreSQL**.

## 🏗️ Arquitectura

### Estructura de Capas (Hexagonal)

```
catalog/
├── domain/                     # Capa de dominio (Reglas de negocio)
│   ├── model/
│   │   ├── Medication.java           # Entidad de dominio: Medicamento
│   │   └── BranchStock.java          # Entidad de dominio: Stock en sucursal
│   └── port/
│       ├── in/                        # Puertos de entrada (casos de uso)
│       │   ├── SearchMedicationUseCase.java
│       │   └── UpdateStockUseCase.java
│       └── out/                       # Puertos de salida (persistencia)
│           └── MedicationRepositoryPort.java
│
├── application/                # Capa de aplicación (Casos de uso)
│   └── CatalogService.java     # Implementa puertos de entrada
│
└── infrastructure/             # Capa de infraestructura (Adaptadores)
    ├── adapter/
    │   ├── in/                 # Adaptadores de entrada (REST)
    │   │   ├── MedicationController.java
    │   │   └── dto/
    │   │       ├── CreateMedicationRequest.java
    │   │       ├── UpdateStockRequest.java
    │   │       ├── MedicationResponse.java
    │   │       ├── StockResponse.java
    │   │       └── StockSearchRequest.java
    │   └── out/                # Adaptadores de salida (JPA)
    │       └── MedicationJpaRepository.java
    ├── entity/                 # Entidades JPA
    │   ├── MedicationEntity.java
    │   ├── BranchStockEntity.java
    │   └── BranchEntity.java
    └── repository/             # Spring Data JPA Repositories
        ├── MedicationSpringDataRepository.java
        ├── BranchStockSpringDataRepository.java
        └── BranchSpringDataRepository.java
```

## 🎯 Funcionalidades Implementadas

### 1. **Buscar Medicamento por Nombre**
- **Endpoint**: `GET /api/medications/search?name={término}`
- **Búsqueda**: Parcial, insensible a mayúsculas/minúsculas (LIKE)
- **Respuesta**: Lista de medicamentos con id, nombre, descripción, unidad
- **Códigos HTTP**: 200 (éxito), 400 (búsqueda vacía)

### 2. **Ver Disponibilidad por Sucursal**
- **Endpoint**: `GET /api/medications/branch/{branchId}/stock`
- **Respuesta**: Lista de medicamentos con cantidad, disponibilidad, unidad
- **Soporta**: Múltiples sucursales, indicador de disponibilidad (cantidad > 0)
- **Códigos HTTP**: 200 (éxito), 400 (id inválido)

### 3. **Admin Crea Medicamento**
- **Endpoint**: `POST /api/medications`
- **Entrada**:
  ```json
  {
    "name": "Paracetamol 500mg",
    "description": "Analgésico y antipirético",
    "unit": "tableta",
    "branchId": 1,
    "initialStock": 100
  }
  ```
- **Validaciones**:
  - Nombre requerido y único
  - Unidad requerida
  - Stock inicial >= 0
  - Nombre máximo 255 caracteres
- **Respuesta**: 201 Created con datos del medicamento creado
- **Códigos HTTP**: 201 (creado), 400 (validación)

### 4. **Admin Edita Disponibilidad**
- **Endpoint**: `PUT /api/medications/{medicationId}/branch/{branchId}/stock`
- **Entrada**:
  ```json
  {
    "medicationId": 1,
    "quantity": 50
  }
  ```
- **Comportamiento**:
  - Crea registro si no existe
  - Actualiza si existe
  - Permite cantidad = 0 (sin stock)
  - No permite cantidades negativas
- **Respuesta**: 204 No Content
- **Códigos HTTP**: 204 (éxito), 400 (validación), 404 (medicamento no encontrado)

## 📋 Base de Datos

### Tablas

```sql
-- Medicamentos
CREATE TABLE medications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    unit VARCHAR(50) NOT NULL
);

-- Sucursales
CREATE TABLE branches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION
);

-- Stock por sucursal
CREATE TABLE branch_stock (
    id BIGSERIAL PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branches(id),
    medication_id BIGINT NOT NULL REFERENCES medications(id),
    quantity INTEGER NOT NULL,
    UNIQUE(branch_id, medication_id)
);
```

## 🧪 Pruebas Unitarias

El módulo incluye **40+ pruebas unitarias** con cobertura completa:

### CatalogServiceTest (16 tests)
- ✅ Búsqueda por nombre
- ✅ Validaciones de entrada
- ✅ Creación de medicamentos
- ✅ Actualización de stock
- ✅ Manejo de excepciones

### MedicationControllerTest (10 tests)
- ✅ Endpoints REST
- ✅ Validación de DTOs
- ✅ Respuestas HTTP correctas
- ✅ Manejo de errores

### MedicationJpaRepositoryTest (12 tests)
- ✅ Búsquedas en BD
- ✅ CRUD operations
- ✅ Queries personalizadas
- ✅ Mapeo domain ↔ entity

### GlobalExceptionHandlerTest (4 tests)
- ✅ Manejo de BusinessException
- ✅ Manejo de ResourceNotFoundException
- ✅ Respuestas de error estandarizadas

**Ejecutar pruebas:**
```bash
mvn test
```

## 📚 Documentación con Swagger/OpenAPI

### Acceder a Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### Características OpenAPI
- ✅ **3 Tags**: Catalog, Inventory
- ✅ **4 Endpoints documentados** con ejemplos JSON
- ✅ **Códigos HTTP**: 200, 201, 204, 400, 404
- ✅ **Esquemas con anotaciones**: `@Schema`, `@Operation`, `@ApiResponse`
- ✅ **Ejemplos de request/response** en cada endpoint
- ✅ **Descripciones detalladas** de parámetros

### Ejemplos Swagger

**Búsqueda de medicamentos:**
```json
GET /api/medications/search?name=paracetamol

Response 200:
[
  {
    "id": 1,
    "name": "Paracetamol 500mg",
    "description": "Analgésico y antipirético",
    "unit": "tableta"
  }
]
```

**Obtener stock:**
```json
GET /api/medications/branch/5/stock

Response 200:
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

## 🛡️ Manejo de Excepciones

El módulo incluye **manejo global de excepciones** (@ControllerAdvice):

### Excepciones Personalizadas
- `BusinessException`: Validaciones de reglas de negocio
- `ResourceNotFoundException`: Recursos no encontrados
- `MethodArgumentNotValidException`: Validación de DTOs

### Respuesta de Error Estándar
```json
{
  "status": 400,
  "message": "El nombre del medicamento es requerido",
  "errorCode": "VALIDATION_ERROR",
  "path": "/api/medications",
  "timestamp": "2026-04-01T08:32:34.123",
  "details": "..."
}
```

## ✨ Características de Calidad

### Validaciones
- ✅ DTOs con anotaciones `@Valid` (Jakarta Validation)
- ✅ Validaciones de reglas de negocio en servicio
- ✅ Mensajes de error descriptivos
- ✅ Códigos de error estándares

### Arquitectura
- ✅ Bajo acoplamiento (puertos y adaptadores)
- ✅ Alta cohesión (responsabilidad única)
- ✅ Facilidad de testing (inyección de dependencias)
- ✅ Preparado para concurrencia futura

### Logging
- ✅ Logs en operaciones críticas
- ✅ Nivel apropiadio (INFO, WARN, ERROR, DEBUG)
- ✅ SLF4J con Lombok `@Slf4j`

### Transacciones
- ✅ `@Transactional` en CatalogService
- ✅ Operaciones de lectura: `readOnly = true`
- ✅ Operaciones de escritura: propagación por defecto

## 🚀 Uso en Desarrollo

### Construcción
```bash
mvn clean build
```

### Tests
```bash
mvn test
```

### Ejecutar aplicación
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Acceder a API
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Base: http://localhost:8080/api/medications

## 📊 Métricas y Monitoreo

### JaCoCo Coverage (Recomendado)
Agregar al pom.xml:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
</plugin>
```

```bash
mvn clean test jacoco:report
```

### SonarQube Analysis
```bash
mvn sonar:sonar
```

## 🔮 Extensiones Futuras

El módulo está diseñado para futuras extensiones:

1. **Tiempo Real (WebSockets)**
   - Notificaciones de cambios de stock
   - Arquitectura preparada (servicios sin dependencias de transporte)

2. **Caché Distribuido**
   - `StockCacheAdapter` ya existe en infraestructura
   - Búsquedas frecuentes: caching inteligente

3. **Auditoría**
   - Logs de cambios de stock
   - Historial de medicamentos

4. **Filtros Avanzados**
   - Por rango de precios
   - Por disponibilidad mínima
   - Ordenamientos personalizados

## 📝 Notas de Implementación

### Decisiones de Arquitectura

1. **Modelos de Dominio Simples**
   - Usar `@Data` y `@Builder` de Lombok
   - Evitar lógica de negocio en entidades JPA

2. **DTOs para Request/Response**
   - Separación clara de contratos
   - Validaciones en DTOs (entrada)
   - Mapping explícito en controlador

3. **Adaptador de Repositorio**
   - Mapeo entity ↔ domain (conversión manual)
   - Abstracción clara de Spring Data
   - Manejo de Optional para nulabilidad

4. **Servicio de Aplicación**
   - Orquesta casos de uso
   - Valida lógica de negocio
   - Maneja transacciones

5. **Controlador**
   - Solo HTTP concerns
   - Mapeo DTO ↔ domain
   - Logging de entrada de datos

## 🔗 Integración con Monolito

El módulo está completamente desacoplado y listo para:
- ✅ Coexistir con otros módulos (users, orders, auctions, logistics)
- ✅ Compartir configuración global (excepciones, Swagger)
- ✅ Usar la misma BD (PostgreSQL)
- ✅ Spring Boot auto-discovery de componentes

## 📞 Soporte

Para preguntas o problemas:
1. Revisar logs en `/target/logs/`
2. Ejecutar tests y validar cobertura
3. Revisar documentación Swagger

---

**Versión**: 1.0.0  
**Última actualización**: Abril 2026  
**Estado**: ✅ Producción
