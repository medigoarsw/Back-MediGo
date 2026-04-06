error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisAuctionParticipantAdapter.java:org/springframework/data/redis/core/StringRedisTemplate#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisAuctionParticipantAdapter.java
empty definition using pc, found symbol in pc: org/springframework/data/redis/core/StringRedisTemplate#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 240
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/out/RedisAuctionParticipantAdapter.java
text:
```scala
package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionParticipantPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.@@StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Almacena participantes de subastas en un Redis Set.
 * Clave: auction:participants:{auctionId}
 */
@Component
@RequiredArgsConstructor
public class RedisAuctionParticipantAdapter implements AuctionParticipantPort {

    private static final String KEY_PREFIX = "auction:participants:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addParticipant(Long auctionId, Long userId) {
        redisTemplate.opsForSet().add(KEY_PREFIX + auctionId, userId.toString());
    }

    @Override
    public boolean isParticipant(Long auctionId, Long userId) {
        return Boolean.TRUE.equals(
            redisTemplate.opsForSet().isMember(KEY_PREFIX + auctionId, userId.toString())
        );
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/data/redis/core/StringRedisTemplate#