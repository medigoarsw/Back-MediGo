package edu.escuelaing.arsw.medigo.auction.domain.port.out;

/**
 * Puerto de salida para el mutex distribuido de pujas.
 * Implementado con Redis SETNX + TTL para garantizar
 * que solo una puja se procese a la vez por subasta.
 */
public interface BidLockPort {
    /**
     * Intenta adquirir lock exclusivo para auctionId.
     * @return true si obtuvo el lock, false si otro hilo lo tiene.
     */
    boolean acquireLock(Long auctionId, String lockValue);

    /**
     * Libera el lock solo si lockValue coincide (evita liberar lock ajeno).
     */
    void releaseLock(Long auctionId, String lockValue);
}
