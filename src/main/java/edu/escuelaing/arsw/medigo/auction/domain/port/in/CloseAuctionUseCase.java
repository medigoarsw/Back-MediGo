package edu.escuelaing.arsw.medigo.auction.domain.port.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;

public interface CloseAuctionUseCase {
    Auction closeAuction(Long auctionId);
    void adjudicateWinner(Long auctionId);
}
