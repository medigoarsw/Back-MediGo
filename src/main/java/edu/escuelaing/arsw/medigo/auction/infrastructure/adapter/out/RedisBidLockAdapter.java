package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.BidLockPort;
import org.springframework.stereotype.Component;
@Component
public class RedisBidLockAdapter implements BidLockPort {
    // TODO Juana: SET auction:lock:{auctionId} {val} NX EX 5
    @Override public boolean acquireLock(Long auctionId, String lockValue) { return true; }
    @Override public void releaseLock(Long auctionId, String lockValue) {}
}