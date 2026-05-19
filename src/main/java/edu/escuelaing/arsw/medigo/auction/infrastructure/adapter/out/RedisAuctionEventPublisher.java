package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisAuctionEventPublisher implements AuctionEventPublisherPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CHANNEL = "auction-events";

    @Override
    public void publish(Long auctionId, AuctionEvent event) {
        try {
            // Publish to Redis instead of direct WebSocket
            redisTemplate.convertAndSend(CHANNEL, event);
            log.info("[Redis] Event {} published to channel {} for auction {}", 
                    event.getType(), CHANNEL, auctionId);
        } catch (Exception e) {
            log.error("[Redis] Failed to publish event: {}", e.getMessage());
        }
    }
}
