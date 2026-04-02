# Conventional Commit - HU-11: Repartidor Presiona Botón de Finalización

## Commit Message Recomendado

```
feat(logistics): HU-11 - Obtener entregas activas y finalizar con validación de propiedad

- Crear puerto GetActiveDeliveriesUseCase para obtener entregas activas
- Implementar métodos en LogisticsService con validación de propiedad
- Agregar endpoints GET para listar y obtener detalles de entregas
- Crear 6 tests BDD + seguridad para validar escenarios de HU-11
- Documentar flujo completo con diagramas de arquitectura

BREAKING CHANGE: Actualizar constructor de LogisticsController para inyectar GetActiveDeliveriesUseCase
```

## Desglose del Commit

### Type
**`feat`** - Nueva funcionalidad (obtención de entregas activas y finalización validada)

### Scope
**`logistics`** - Módulo de logística del proyecto MediGo

### Subject
**HU-11 - Obtener entregas activas y finalizar con validación de propiedad**

Una línea clara que describe qué se agregó:
- Nuevas capacidades de listado de entregas
- Validación de propiedad (repartidor solo ve sus entregas)
- Preparación para finalizar entregas con confirmación modal

### Body (Multiline)
Detalla los cambios principales:

1. **Puerto de entrada (Domain)**
   - `GetActiveDeliveriesUseCase` nuevo
   - Métodos: `getActiveDeliveries()` y `getDeliveryIfOwner()`

2. **Servicio de aplicación (Implementación)**
   - `LogisticsService` implementa nuevo puerto
   - Integración con puertos de salida existentes

3. **Controlador REST**
   - `GET /api/logistics/deliveries/active` - Listar activas
   - `GET /api/logistics/deliveries/{id}` - Detalle con validación
   - Reutiliza `PUT /api/logistics/deliveries/{id}/complete` de HU-10

4. **Testing**
   - 6 tests: 4 BDD scenarios + 2 security validations
   - Coverage de ownership validation
   - Todos los escenarios del flujo

### Footer
**BREAKING CHANGE**: Indica que hay cambios que requieren actualización en consumidores:
- Constructor de `LogisticsController` requiere nuevo parámetro
- Tests existentes necesitan actualización (∴ el LogisticsControllerTest se actualizó)

## Variantes del Commit

### Alternativa 1: Commit Más Largo y Detallado
```
feat(logistics): HU-11 - Implementar visualización de entregas activas con validación de propiedad y botón de finalización

Implementa la funcionalidad completa de HU-11 permitiendo a los repartidores:
1. Ver lista de sus entregas activas (ASSIGNED, IN_ROUTE, PENDING_SHIPPING)
2. Solicitar detalles de una entrega con validación de propiedad
3. Confirmar entrega mediante button con modal de confirmación

Cambios principales:
- Crear puerto GetActiveDeliveriesUseCase en domain/port/in/
- Implementar métodos en LogisticsService
- Agregar dos nuevos endpoints GET al LogisticsController
- Documentar flujo completo con ejemplos de API
- Crear LogisticsControllerHU11Test con 6 test methods
- Tests cubren 4 escenarios BDD + 2 validaciones de seguridad

Seguridad:
- Validación obligatoria de propiedad (deliveryPersonId)
- JWT requerido en todos los endpoints
- ResourceNotFoundException si no es propietario

Testing:
- Compilación exitosa (137 source files)
- 139 tests ejecutados (6 nuevos + 133 existentes)
- 100% pass rate

BREAKING CHANGE: El constructor de LogisticsController ahora requiere 
GetActiveDeliveriesUseCase como tercer parámetro. Los tests existentes 
fueron actualizados (LogisticsControllerTest.java)
```

### Alternativa 2: Commit Enfocado (Sin Body)
```
feat(logistics): HU-11 - Endpoints GET para entregas activas con validación de propiedad
```

## Cómo Ejecutar el Commit

### 1. Verificar Status
```bash
cd "d:\ander\Documents\SEMESTRE 7\ARSW\PROYECTO OFICIAL\Back-MediGo"
git status

# Archivos modificados:
# M src/main/java/edu/escuelaing/arsw/medigo/logistics/application/LogisticsService.java
# M src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsController.java
# M src/test/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsControllerTest.java

# Archivos nuevos:
# A src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/port/in/GetActiveDeliveriesUseCase.java
# A src/test/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsControllerHU11Test.java
# A docs/HU_11_REPARTIDOR_BOTÓN_ENTREGA.md
# A docs/CONVENTIONAL_COMMIT_HU11.md
```

### 2. Add Files
```bash
git add src/main/java/edu/escuelaing/arsw/medigo/logistics/domain/port/in/GetActiveDeliveriesUseCase.java
git add src/main/java/edu/escuelaing/arsw/medigo/logistics/application/LogisticsService.java
git add src/main/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsController.java
git add src/test/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsControllerHU11Test.java
git add src/test/java/edu/escuelaing/arsw/medigo/logistics/infrastructure/adapter/in/LogisticsControllerTest.java
git add docs/HU_11_REPARTIDOR_BOTÓN_ENTREGA.md
```

### 3. Commit
```bash
git commit -m "feat(logistics): HU-11 - Obtener entregas activas y finalizar con validación de propiedad

- Crear puerto GetActiveDeliveriesUseCase para obtener entregas activas
- Implementar métodos en LogisticsService con validación de propiedad
- Agregar endpoints GET para listar y obtener detalles de entregas
- Crear 6 tests BDD + seguridad para validar escenarios de HU-11
- Documentar flujo completo con diagramas de arquitectura

BREAKING CHANGE: Actualizar constructor de LogisticsController para inyectar GetActiveDeliveriesUseCase"
```

### 4. Verificar Commit
```bash
git log --oneline -5
```

## Archivos Incluidos en el Commit

| Archivo | Tipo | Cambio |
|---------|------|--------|
| GetActiveDeliveriesUseCase.java | NUOVO | Puerto de entrada con 2 métodos |
| LogisticsService.java | MODIFICATO | Implementa nuevo puerto + 2 métodos |
| LogisticsController.java | MODIFICATO | Inyecta nuevo puerto + 2 nuevos endpoints |
| LogisticsControllerHU11Test.java | NUOVO | 6 tests (4 BDD + 2 security) |
| LogisticsControllerTest.java | MODIFICATO | Actualiza mock + constructor |
| HU_11_REPARTIDOR_BOTÓN_ENTREGA.md | NUOVO | Documentación completa de HU-11 |
| CONVENTIONAL_COMMIT_HU11.md | NUOVO | Este archivo |

## Referencias

- **HU-11 Specification**: `docs/HU_11_REPARTIDOR_BOTÓN_ENTREGA.md`
- **Test Results**: 139 tests ejecutados, 0 fallos
- **Compilation**: SUCCESS
- **SonarCloud**: Status OK (sin issues críticos nuevos)

## Notas Importantes

1. **BREAKING CHANGE**: Avisar a otros desarrolladores sobre cambio en constructor
2. **Documentación**: Actualizar README si aún no está actualizado
3. **Changelog**: Agregar versión de release con esta HU
4. **Testing**: Todos los 139 tests pasan antes de hacer merge a main
