package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class AuctionClosedException extends RuntimeException {
    public AuctionClosedException() {
        super("La subasta ha finalizado. No se aceptan nuevas pujas.");
    }
}
