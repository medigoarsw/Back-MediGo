package edu.escuelaing.arsw.medigo.auction.domain.exception;

public class UserNotJoinedException extends RuntimeException {
    public UserNotJoinedException(Long userId, Long auctionId) {
        super("El usuario " + userId + " no se ha unido a la subasta " + auctionId);
    }
}
