package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class AuctionNotEditableException extends RuntimeException {
    public AuctionNotEditableException(String status) {
        super("No se puede editar una subasta en estado: " + status);
    }
}
