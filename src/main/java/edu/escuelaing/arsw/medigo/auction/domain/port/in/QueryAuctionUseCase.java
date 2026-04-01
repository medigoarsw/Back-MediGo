package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import java.util.List;

public interface QueryAuctionUseCase {
    List<Auction> getActiveAuctions();
    Auction       getAuctionById(Long id);
    List<Bid>     getBidHistory(Long auctionId);
}
