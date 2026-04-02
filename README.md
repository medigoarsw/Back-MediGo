# Back-MediGo

Sistema backend para la plataforma **MediGo** - Gestión de catálogo, órdenes y logística de medicamentos.

## 📋 Descripción del Proyecto

MediGo es una plataforma integral de e-commerce especializada en medicamentos que permite:
- **Administradores**: Crear y gestionar medicamentos en el catálogo
- **Clientes**: Buscar y comprar medicamentos con entrega a domicilio
- **Gestión Logística**: Seguimiento de pedidos y entregas

### Stack Tecnológico

| Categoría | Tecnología |
|-----------|------------|
| **Framework** | Spring Boot 3.1.5 |
| **Autenticación** | Spring Security 6 + JWT |
| **ORM** | JPA/Hibernate |
| **Base de Datos** | PostgreSQL (producción), H2 (tests) |
| **Testing** | JUnit 5 + Mockito |
| **API Docs** | Swagger/OpenAPI 3.0 |
| **Build** | Maven 3.9+ |
| **Java** | JDK 17+ |

---

## ✅ Historias de Usuario Completadas

### HU-01: Login de Usuario
- **Status**: ✅ COMPLETADA
- **Resumen**: Autenticación con JWT, login exitoso y rechazo de credenciales inválidas
- **Tests**: 3/3 PASSING
- **Documentación**: [HU-01 Login](docs/QUICK_REFERENCE.md#hu-01-login)

### HU-02: Autenticación 
- **Status**: ✅ COMPLETADA
- **Resumen**: Generación y validación de JWT, gestión de sesiones
- **Tests**: 4/4 PASSING
- **Documentación**: [README Autenticación](docs/README_MODULO_AUTENTICACION.md)

### HU-03: Búsqueda de Medicamentos
- **Status**: ✅ COMPLETADA
- **Resumen**: Clientes buscan medicamentos por nombre, ven detalles y stock
- **Tests**: 5/5 PASSING
- **Documentación**: [Entrega Catálogo](docs/ENTREGA_CATALOGO_INVENTARIO.md)

### HU-04: Creación de Pedidos
- **Status**: ✅ COMPLETADA  
- **Resumen**: Clientes crean órdenes de compra con medicamentos, validación de stock
- **Tests**: 8/8 PASSING
- **Documentación**: [Guía Testing Catálogo](docs/GUIA_TESTING_CATALOGO.md)

### HU-05: Confirmación de Órdenes
- **Status**: ✅ COMPLETADA
- **Resumen**: Confirmación de pago, generación de número de orden, cálculo de total
- **Tests**: 12/12 PASSING
- **Documentación**: [Guía Testing](docs/GUIA_TESTING.md)

### HU-06: Asignación de Ruta de Entrega + PASO-1: Seguridad
- **Status**: ✅ COMPLETADA
- **Resumen**: Asignación automática de rutas con transecciones, SecureRandom para generación de números
- **Tests**: 13/13 PASSING
- **Security**: ✅ Fixed weak PRNG (SecureRandom), 0 SonarCloud hotspots
- **Documentación**: [Resumen Final](docs/RESUMEN_FINAL.md)

### ⭐ HU-07: Administrador Crea Medicamento (NUEVA - COMPLETADA)
- **Status**: ✅ COMPLETADA
- **Resumen**: Administradores agregan medicamentos al catálogo con precio y stock
- **Escenarios**: 4/4 PASSING (incluidos en CatalogServiceTest)
- **Tests**: 19/19 PASSING (4 nuevos + 15 existentes)
- **Security**: ✅ @PreAuthorize("hasRole('ADMIN')"), validaciones completas
- **Build**: ✅ SUCCESS
- **Documentación**: [HU-07 Crear Medicamento](docs/HU_07_CREAR_MEDICAMENTO.md)
- **Commit Guide**: [Conventional Commit HU-07](docs/CONVENTIONAL_COMMIT_HU07.md)

---

## 🚀 Quick Start

### Requisitos Previos
```bash
- Java 17+
- Maven 3.9+
- PostgreSQL 14+ (opcional, usa H2 para tests/desarrollo)
```

### Compilación y Tests

```bash
# 1. Clonar el repositorio
git clone <repo-url>
cd Back-MediGo

# 2. Compilar el proyecto
mvn clean compile

# 3. Ejecutar todos los tests
mvn test

# 4. Ejecutar solo tests de HU-07
mvn test -Dtest=CatalogServiceTest

# 5. Build completo (compilar + tests)
mvn clean package
```

### Ejecución del Servidor

```bash
# Desarrollo local
mvn spring-boot:run

# Producción
mvn clean package
java -jar target/medigo-*.jar
```

**Puerto por defecto**: `http://localhost:8080`

---

## 📁 Estructura del Proyecto

```
Back-MediGo/
├── src/
│   ├── main/java/edu/escuelaing/arsw/medigo/
│   │   ├── MediGoApplication.java          # Clase principal Spring Boot
│   │   ├── auction/                        # Módulo de subastas
│   │   ├── catalog/                        # Módulo de catálogo (HU-03, HU-07)
│   │   │   ├── domain/
│   │   │   │   ├── model/   
│   │   │   │   │   ├── Medication.java     # Entidad de medicamento (✅ con price)
│   │   │   │   │   └── Branch.java
│   │   │   │   └── port/
│   │   │   │       └── in/
│   │   │   │           ├── SearchMedicationUseCase.java
│   │   │   │           └── CreateMedicationUseCase.java  # ✅ NUEVO (HU-07)
│   │   │   ├── application/
│   │   │   │   └── CatalogService.java     # ✅ Implementa CreateMedicationUseCase
│   │   │   └── infrastructure/
│   │   │       ├── adapter/in/
│   │   │       │   ├── MedicationController.java  # ✅ Endpoint POST /api/medications
│   │   │       │   └── dto/
│   │   │       │       ├── CreateMedicationRequest.java  # ✅ Con validación de price
│   │   │       │       └── MedicationResponse.java       # ✅ Incluye price
│   │   │       └── entity/
│   │   │           └── MedicationEntity.java  # ✅ Con columna price en BD
│   │   ├── logistics/                       # Módulo de logística (HU-06)
│   │   ├── orders/                          # Módulo de órdenes (HU-04, HU-05)
│   │   ├── shared/                          # Código compartido
│   │   └── users/                           # Módulo de usuarios (HU-01, HU-02)
│   └── test/java/...                        # Tests JUnit 5 + Mockito
└── docs/
    ├── HU_07_CREAR_MEDICAMENTO.md          # ✅ Documentación HU-07
    ├── CONVENTIONAL_COMMIT_HU07.md         # ✅ Guía de commit
    ├── GUIA_TESTING.md
    ├── GUIA_TESTING_CATALOGO.md
    ├── QUICK_REFERENCE.md
    └── RESUMEN_FINAL.md
```

---

## 🧪 Coverage de Tests

### Resumen Actual
```
Total Tests: 19 (CatalogServiceTest - HU-07 incluida)
Passing: 19/19 ✅
Failing: 0 ✅
Build: SUCCESS ✅
```

### HU-07 Tests (4 escenarios BDD)

| # | Escenario | Test | Status |
|---|-----------|------|--------|
| 1️⃣ | Crear medicamento exitosamente | `testCreateMedicationSuccessfully()` | ✅ |
| 2️⃣ | Campos obligatorios vacíos | `testCreateMedicationWithEmptyName()` | ✅ |
| 3️⃣ | Precio inválido (≤ 0) | `testCreateMedicationWithInvalidPrice()` | ✅ |
| 4️⃣ | Visible en búsqueda de clientes | `testCreatedMedicationVisibleInSearch()` | ✅ |

---

## 🔒 Seguridad

### HU-07 Medidas Implementadas
- ✅ **Autenticación**: JWT Token requerido
- ✅ **Autorización**: Solo rol ADMIN puede crear medicamentos (`@PreAuthorize`)
- ✅ **Validación de Entrada**: 
  - Nombre: `@NotBlank`
  - Presentación: `@NotBlank`
  - Precio: `@DecimalMin("0.01")` (+ validación en servicio)
  - Stock: `@Positive`
- ✅ **Valores Monetarios**: BigDecimal (no float/double)
- ✅ **SonarCloud**: 0 security hotspots nuevos

---

## 📝 API Documentation

### Swagger/OpenAPI

Swagger UI disponible en:
```
http://localhost:8080/swagger-ui.html
```

### Endpoint HU-07

**POST** `/api/medications`
```json
{
  "name": "Paracetamol 500mg",
  "description": "Analgésico y antipirético",
  "unit": "Tabletas",
  "price": 5000.00,
  "branchId": 1,
  "initialStock": 100
}
```

**Response (201 Created)**:
```json
{
  "id": 10,
  "name": "Paracetamol 500mg",
  "description": "Analgésico y antipirético",
  "unit": "Tabletas",
  "price": 5000.00,
  "stock": 100,
  "branchId": 1,
  "createdAt": "2024-01-15T16:05:59Z"
}
```

---

## 📚 Documentación Completa

- [HU-07: Crear Medicamento](docs/HU_07_CREAR_MEDICAMENTO.md) - ✅ NUEVA
- [Conventional Commit HU-07](docs/CONVENTIONAL_COMMIT_HU07.md) - ✅ NUEVA
- [Guía API Autenticación](docs/GUIA_API_AUTENTICACION.md)
- [Resumen Final](docs/RESUMEN_FINAL.md)
- [Quick Reference](docs/QUICK_REFERENCE.md)
- [Arquitectura Autenticación](docs/ARQUITECTURA_AUTENTICACION.md)

---

## 🛣️ Roadmap

### Completadas ✅
- HU-01: Login
- HU-02: Autenticación  
- HU-03: Búsqueda de medicamentos
- HU-04: Creación de pedidos
- HU-05: Confirmación de órdenes
- HU-06: Asignación de ruta + Seguridad (PASO-1)
- **HU-07: Administrador crea medicamento** ⭐

### En Progreso ⏳
- HU-08: Seguimiento de pedidos
- HU-09: Historial de compras

### Planeadas 📋
- HU-10: Historial de entregas
- HU-11: Reportes administrativos
- HU-12: Notificaciones por email

---

## 🤝 Contributing

1. Crear rama feature: `git checkout -b feature/hu-XX-descripcion`
2. Implementar cambios
3. Ejecutar tests: `mvn test`
4. Commit con conventional commits: `git commit -m "feat(modulo): descripcion"`
5. Push a rama: `git push origin feature/hu-XX-descripcion`
6. Crear Pull Request

### Estándares de Código
- ✅ Todos los tests passing
- ✅ Build SUCCESS
- ✅ 0 SonarCloud issues nuevos
- ✅ JaCoCo coverage > 70%
- ✅ Javadoc para métodos públicos
- ✅ Messages en español (comentarios y logs)

---

## 📊 Métricas

| Métrica | Valor |
|---------|-------|
| **Total Tests** | 19+ |
| **Pass Rate** | 100% ✅ |
| **Build Status** | SUCCESS ✅ |
| **Java Version** | 17+ |
| **Code Coverage** | >70% (JaCoCo) |
| **Security Issues** | 0 (SonarCloud) |

---

## 📞 Contacto & Soporte

Para preguntas o reportar bugs:
1. Revisa la documentación en `/docs`
2. Busca en los tests para ejemplos
3. Crea un issue con detalles del problema

---

## 📄 Licencia

Este proyecto es parte del curso ARSW (Arquitectura de Software) 2024.

---

**Última actualización**: Enero 2024 - HU-07 COMPLETADA ✅