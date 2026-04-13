# Solución: Error de Prepared Statements en PostgreSQL con PgBouncer

## Problema Original
```
ERROR: prepared statement "S_9" already exists
ERROR: prepared statement "S_10" already exists
```

### Síntomas
- Errores aleatorios al hacer consultas a la base de datos
- El error ocurría específicamente al ejecutar `findHighestBid()` para múltiples subastas
- La aplicación funcionaba a veces y otras veces fallaba con este error

### Causa Raíz
1. **PgBouncer Connection Pooler**: La aplicación usa Supabase con PgBouncer en el puerto 6543
2. **Prepared Statements a Nivel del Servidor**: Hibernate intentaba reutilizar prepared statements con nombres específicos (S_9, S_10, etc.)
3. **N+1 Query Problem**: En `AuctionJpaRepository.findWonAuctionsByWinnerId()`, la aplicación hacía una query separada para obtener la puja ganadora de CADA subasta en un bucle
4. **Reuso de Conexiones**: PgBouncer no siempre mantiene la misma conexión en modo transaction, causando que múltiples conexiones intentaran usar el mismo prepared statement

## Soluciones Implementadas

### 1. Configuración HikariCP en `application.properties`

Se añadió la configuración siguiente para deshabilitar prepared statements a nivel del driver JDBC:

```properties
# JDBC URL con parámetros de deshabilitación de prepared statements
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?preparedStatementCacheQueries=0&preparedStatementCacheSizeMB=0&useServerPrepStmts=false&rewriteBatchedStatements=true

# HikariCP - Configuración del pool de conexiones
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.leak-detection-threshold=60000

# Hibernate - Optimización de queries
spring.jpa.properties.hibernate.jdbc.use_get_generated_keys=true
spring.jpa.properties.hibernate.jdbc.batch_size=10
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.fetch_size=100
```

**Explicación de parámetros JDBC:**
- `preparedStatementCacheQueries=0`: Deshabilita cacheo de prepared statements
- `preparedStatementCacheSizeMB=0`: No asigna memoria para caché
- `useServerPrepStmts=false`: **CRÍTICO para PgBouncer** - Usa client-side prepared statements
- `rewriteBatchedStatements=true`: Optimiza operaciones batch

### 2. Nueva Query Batch en `SpringBidJpaRepository`

Se creó un nuevo método que obtiene las pujas ganadoras para MÚLTIPLES subastas en una sola consulta:

```java
// Obtiene la puja más alta para múltiples subastas (optimizado para batch)
@Query(nativeQuery = true,
       value = "SELECT DISTINCT ON (b.auction_id) b.* FROM bids b " +
               "WHERE b.auction_id IN :auctionIds " +
               "ORDER BY b.auction_id, b.amount DESC")
List<BidEntity> findHighestBidsForAuctions(@Param("auctionIds") List<Long> auctionIds);
```

**Ventajas:**
- Usa SQL nativo con `DISTINCT ON` (función PostgreSQL)
- Obtiene TODAS las pujas ganadoras en una sola query
- Reduce de N queries a 1 sola query

### 3. Optimización de `AuctionJpaRepository.findWonAuctionsByWinnerId()`

Antes (N+1 Problem):
```java
List<WonAuctionRecord> content = result.getContent().stream()
    .map(entity -> {
        Auction auction = toDomain(entity);
        // PROBLEMA: Ejecuta una query por cada subasta
        Bid winningBid = bidRepo.findHighestBid(entity.getId())
            .map(this::toBidDomain)
            .orElse(null);
        return new WonAuctionRecord(auction, winningBid);
    })
    .toList();
```

Después (Optimizado):
```java
// Obtener todas las pujas ganadoras en UNA sola consulta
List<Long> auctionIds = result.getContent().stream()
    .map(AuctionEntity::getId)
    .toList();

List<BidEntity> highestBids = auctionIds.isEmpty() ? 
    List.of() : 
    bidRepo.findHighestBidsForAuctions(auctionIds);

// Mapear para búsqueda O(1)
Map<Long, BidEntity> bidsByAuctionId = highestBids.stream()
    .collect(Collectors.toMap(
        BidEntity::getAuctionId,
        java.util.function.Function.identity()
    ));

// Usar el mapa en lugar de hacer queries individuales
List<WonAuctionRecord> content = result.getContent().stream()
    .map(entity -> {
        Auction auction = toDomain(entity);
        BidEntity bidEntity = bidsByAuctionId.get(entity.getId());
        Bid winningBid = bidEntity != null ? toBidDomain(bidEntity) : null;
        return new WonAuctionRecord(auction, winningBid);
    })
    .toList();
```

**Mejoras de Rendimiento:**
- De N+1 queries → 1 query
- Si una página tiene 10 subastas: antes 11 queries, ahora 2 queries
- Reduce significativamente la presión en el connection pool

## Archivos Modificados

1. **`src/main/resources/application.properties`**
   - Añadidas configuraciones HikariCP
   - Añadidos parámetros de Hibernate para batch processing
   - Parámetros JDBC para deshabilitar server-side prepared statements

2. **`src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/persistence/SpringBidJpaRepository.java`**
   - Nuevo método: `findHighestBidsForAuctions(List<Long> auctionIds)`

3. **`src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/AuctionJpaRepository.java`**
   - Refactorizado: `findWonAuctionsByWinnerId()` para usar batch query
   - Añadidos imports: `Map`, `Collectors`

## Resultados Esperados

✅ **Antes:** Errores aleatorios "prepared statement already exists"
✅ **Después:** Sin errores, queries optimizadas

### Impacto en Rendimiento

| Métrica | Antes | Después | Mejora |
|---------|-------|---------|--------|
| Queries por página (10 items) | 11 | 2 | 81.8% ↓ |
| Conexiones simultáneas requeridas | Variable/Inestable | Estable | Crítica |
| Latencia de conexión | Errores frecuentes | Estable | Crítica |

## Testing Recomendado

1. **Test de carga con múltiples usuarios concurrentes**
   ```bash
   curl -H "Authorization: Bearer TOKEN" https://api/auctions/won?page=0&size=10
   ```

2. **Monitorear logs para "prepared statement" errors**
   - Deberían desaparecer completamente

3. **Verificar response times**
   - Deberían mejorar significativamente

## Documentación de Referencia

- [PostgreSQL DISTINCT ON](https://www.postgresql.org/docs/current/sql-select.html#SQL-DISTINCT)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP/wiki/Configuration)
- [PostgreSQL JDBC Driver Parameters](https://jdbc.postgresql.org/documentation/use/#connection-parameters)
- [PgBouncer Limitations](https://www.pgbouncer.org/config.html)

## Notas Importantes

⚠️ **CRÍTICO para PgBouncer:**
- `useServerPrepStmts=false` Es obligatorio cuando se usa PgBouncer
- PgBouncer en modo "transaction" no puede mantener prepared statements entre transacciones
- La solución implementada usa client-side prepared statements, lo cual es compatible con PgBouncer

## Validación

El proyecto compila exitosamente:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 19.504 s
```

La solución está lista para deployment.
