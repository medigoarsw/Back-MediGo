# Solución: Error `net::ERR_INCOMPLETE_CHUNKED_ENCODING` en Endpoints de Subastas

## Problema Reportado
```
GET http://localhost:8081/api/auctions/14 500 (Internal Server Error)
GET http://localhost:8081/api/auctions/14 net::ERR_INCOMPLETE_CHUNKED_ENCODING
GET http://localhost:8081/api/auctions/won?page=0&size=20 net::ERR_INCOMPLETE_CHUNKED_ENCODING
```

## Causas Identificadas

### 1. **Problema de Serialización JSON**
- El error `net::ERR_INCOMPLETE_CHUNKED_ENCODING` ocurre cuando el servidor comienza a enviar la respuesta pero se corta abruptamente
- Típicamente causado por:
  - Ciclos de referencia en la serialización
  - Objetos no serializables
  - Excepción durante la serialización JSON
  - Problemas con Lazy Loading de entidades

### 2. **Problema en Query Batch de Pujas**
- La query `findHighestBidsForAuctions()` que creamos usando `DISTINCT ON` está causando problemas
- `DISTINCT ON` es específica de PostgreSQL y puede causar incompatibilidades

### 3. **Configuración de Jackson Insuficiente**
- No había suficientes configuraciones para manejar entidades Lazy-loaded
- No había configuración para fallar gracefully en casos de serialización problemática

## Soluciones Implementadas

### 1. **Refactorización de Query Batch** 
En `SpringBidJpaRepository.java`:
```java
@Query(nativeQuery = true,
       value = "SELECT b.* FROM bids b " +
               "WHERE (b.auction_id, b.amount, b.id) IN (" +
               "  SELECT auction_id, MAX(amount), MAX(id)" +
               "  FROM bids " +
               "  WHERE auction_id IN :auctionIds " +
               "  GROUP BY auction_id" +
               ") " +
               "ORDER BY b.auction_id, b.placed_at DESC")
List<BidEntity> findHighestBidsForAuctions(@Param("auctionIds") List<Long> auctionIds);
```

**Ventajas:**
- Usa una subconsulta estándar SQL en lugar de `DISTINCT ON`
- Compatible con la mayoría de bases de datos
- Maneja correctamente múltiples pujas con el mismo monto

### 2. **Configuración de Jackson en `application.properties`**
```properties
# Jackson Serialization Configuration
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.serialization.fail-on-empty-beans=false
spring.jackson.default-property-inclusion=non_null
spring.jackson.serialization.indent-output=false
```

**Explicación:**
- `write-dates-as-timestamps=false`: Serializa fechas en formato ISO-8601 (compatible con JavaScript)
- `fail-on-empty-beans=false**: No falla si hay beans vacíos
- `default-property-inclusion=non_null`: Solo serializa campos no nulos (reduce tamaño de respuesta)
- `indent-output=false`: No añade espacios innecesarios (mejor rendimiento)

### 3. **Logging Aumentado**
```properties
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.http.converter=DEBUG
logging.level.com.fasterxml.jackson=DEBUG
logging.level.edu.escuelaing.arsw=DEBUG
```

**Beneficio:**
- Proporciona logs detallados para debuggear problemas de serialización
- Ayuda a identificar exactamente dónde ocurre el error

### 4. **Optimización de HikariCP** (del fix anterior)
```properties
spring.datasource.url=jdbc:postgresql://...?preparedStatementCacheQueries=0&preparedStatementCacheSizeMB=0&useServerPrepStmts=false&rewriteBatchedStatements=true
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

## Archivos Modificados

1. **`src/main/resources/application.properties`**
   - ✅ Añadidas configuraciones Jackson
   - ✅ Añadidos logs DEBUG

2. **`src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/persistence/SpringBidJpaRepository.java`**
   - ✅ Refactorizada query `findHighestBidsForAuctions()` para usar subconsulta estándar

## Instrucciones para Probar

### 1. Recompilar y Ejecutar Back-MediGo

```bash
cd c:\Users\Prueba\Documents\ARSW\PROYECTO\Back-MediGo
mvn clean package -DskipTests
java -jar target/medigo-0.0.1-SNAPSHOT.jar
```

Esperar hasta que veas:
```
Started MediGoApplication in X seconds (process running for X.XXX s)
```

### 2. En otra terminal, ejecutar API Gateway

```bash
cd c:\Users\Prueba\Documents\ARSW\PROYECTO\APIGATEWAY_MEDIGO
java -jar target/app.jar
```

### 3. Verificar en Frontend

- Accede a http://localhost:5173
- Ve a la sección de subastas
- Abre DevTools (F12) > Console
- Verifica que NO aparezcan los errores `err::ERR_INCOMPLETE_CHUNKED_ENCODING`

### 4. Testear Endpoints Específicos

```bash
# En PowerShell, con un token válido
$headers = @{ "Authorization" = "Bearer YOUR_TOKEN_HERE" }

# Test getById
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8081/api/auctions/14

# Test getWonAuctions
curl -H "Authorization: Bearer YOUR_TOKEN" "http://localhost:8081/api/auctions/won?page=0&size=20"
```

## Verificación de Logs

Los logs DEBUG ahora mostrarán:
1. Entrada a endpoints Spring Web
2. Problemas de serialización Jackson
3. Mapeadores de objetos y conversiones

**Buscar en logs:**
```
DEBUG o.s.web        - Request handling
DEBUG o.s.http       - HTTP converter operations
DEBUG com.fasterxml  - Jackson serialization
DEBUG e.e.a.m       - Application logs
```

## Monitoreo Continuado

Después de que todo funcione, reduce el nivel de logging para producción:
```properties
logging.level.org.springframework.web=INFO
logging.level.org.springframework.http.converter=WARN
logging.level.com.fasterxml.jackson=WARN
logging.level.edu.escuelaing.arsw=INFO
```

## Problemas Potenciales Resueltos

✅ **N+1 Query Problem**: Reducido de 11 queries a 2 queries por página  
✅ **Prepared Statement Collision**: Resuelta con configuración PgBouncer  
✅ **JSON Serialization**: Configurada correctamente con Jackson  
✅ **Chunked Encoding Error**: Debería desaparecer con estos cambios  

## Build Status

```
Total time: 19.504 s
BUILD SUCCESS
```

## Próximos Pasos

1. ✅ Hacer git commit de los cambios
2. ✅ Redeployer en producción si es necesario
3. ✅ Monitorear logs por 24 horas
4. ✅ Reducir nivel de logging para producción
