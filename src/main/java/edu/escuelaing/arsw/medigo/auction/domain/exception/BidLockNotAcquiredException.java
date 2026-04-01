package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class BidLockNotAcquiredException extends RuntimeException {
    public BidLockNotAcquiredException() {
        super("No se pudo procesar la puja en este momento. Intenta de nuevo.");
    }
}
