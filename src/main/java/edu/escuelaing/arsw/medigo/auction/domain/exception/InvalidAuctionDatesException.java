package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class InvalidAuctionDatesException extends RuntimeException {
    public InvalidAuctionDatesException(String msg) {
        super(msg);
    }
}
