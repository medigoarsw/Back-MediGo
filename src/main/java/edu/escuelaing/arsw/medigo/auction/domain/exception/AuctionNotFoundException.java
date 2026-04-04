package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class AuctionNotFoundException extends RuntimeException {
    public AuctionNotFoundException(Long id) {
        super("Subasta no encontrada con id: " + id);
    }
}
