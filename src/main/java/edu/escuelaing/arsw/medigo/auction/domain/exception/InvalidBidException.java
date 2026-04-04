package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class InvalidBidException extends RuntimeException {
    public InvalidBidException(String msg) {
        super(msg);
    }
}
