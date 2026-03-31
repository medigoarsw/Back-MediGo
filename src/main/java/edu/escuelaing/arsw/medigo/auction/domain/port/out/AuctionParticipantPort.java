package edu.escuelaing.arsw.medigo.auction.domain.port.out;

public interface AuctionParticipantPort {
    void addParticipant(Long auctionId, Long userId);
    boolean isParticipant(Long auctionId, Long userId);
}
