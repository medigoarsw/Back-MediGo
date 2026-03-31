package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class AuctionAlreadyExistsException extends RuntimeException {
    public AuctionAlreadyExistsException(Long medicationId) {
        super("Este medicamento ya tiene una subasta programada o activa. medicationId=" + medicationId);
    }
}
