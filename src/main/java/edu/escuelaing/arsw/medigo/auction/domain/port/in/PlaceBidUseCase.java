package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.math.BigDecimal;

public interface PlaceBidUseCase {

    /**
     * Coloca una puja de forma thread-safe usando Redis SETNX como mutex distribuido.
     * Lanza BidLockNotAcquiredException si hay otra puja en proceso.
     * Lanza InvalidBidException si el monto no supera el actual.
     * Lanza AuctionClosedException si la subasta ya no acepta pujas.
     */
    Bid placeBid(Long auctionId, Long userId, String userName, BigDecimal amount);
}
