error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisBidLockAdapter.java:org/springframework/data/redis/core/StringRedisTemplate#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisBidLockAdapter.java
empty definition using pc, found symbol in pc: org/springframework/data/redis/core/StringRedisTemplate#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 264
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisBidLockAdapter.java
text:
```scala
package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.port.out.BidLockPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.@@StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Implementacion del mutex distribuido usando Redis SETNX.
 *
 * Patron: SET key value NX EX 5
 *   - NX: solo escribe si la clave NO existe (atomico)
 *   - EX 5: TTL de 5 segundos (auto-libera si el proceso muere)
 *
 * Clave Redis: auction:lock:{auctionId}
 * Valor: lockValue unico (UUID) para evitar liberar lock ajeno
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBidLockAdapter implements BidLockPort {

    private static final String KEY_PREFIX = "auction:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean acquireLock(Long auctionId, String lockValue) {
        String key = KEY_PREFIX + auctionId;
        Boolean acquired = redisTemplate
                .opsForValue()
                .setIfAbsent(key, lockValue, LOCK_TTL);

        boolean result = Boolean.TRUE.equals(acquired);
        log.debug("Lock {} para subasta {}: {}",
                result ? "ADQUIRIDO" : "NO DISPONIBLE", auctionId, key);
        return result;
    }

    @Override
    public void releaseLock(Long auctionId, String lockValue) {
        String key = KEY_PREFIX + auctionId;
        String current = redisTemplate.opsForValue().get(key);

        // Solo libera si el valor coincide (evita liberar lock de otro hilo)
        if (lockValue.equals(current)) {
            redisTemplate.delete(key);
            log.debug("Lock liberado para subasta {}", auctionId);
        } else {
            log.warn("Intento de liberar lock ajeno para subasta {}. " +
                     "expected={} actual={}", auctionId, lockValue, current);
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/data/redis/core/StringRedisTemplate#