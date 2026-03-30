package edu.escuelaing.arsw.medigo.auction.domain.port.out;
public interface BidLockPort {
    boolean acquireLock(Long auctionId, String lockValue);
    void releaseLock(Long auctionId, String lockValue);
}