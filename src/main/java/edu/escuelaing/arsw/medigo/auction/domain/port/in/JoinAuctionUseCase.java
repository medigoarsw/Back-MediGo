package edu.escuelaing.arsw.medigo.auction.domain.port.in;

public interface JoinAuctionUseCase {
    /**
     * Registra la participacion del afiliado en la subasta.
     * Lanza exception si la subasta no esta ACTIVE.
     */
    void joinAuction(Long auctionId, Long userId);
}
