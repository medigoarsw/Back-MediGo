package edu.escuelaing.arsw.medigo.auction.domain.port.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;

/**
 * Puerto de salida para publicar eventos de subasta en tiempo real.
 * Implementado con Spring Messaging + WebSocket STOMP.
 * Topic de destino: /topic/auction/{auctionId}
 */
public interface AuctionEventPublisherPort {
    void publish(Long auctionId, AuctionEvent event);
}
