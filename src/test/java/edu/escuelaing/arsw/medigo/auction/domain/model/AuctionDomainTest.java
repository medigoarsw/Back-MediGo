package edu.escuelaing.arsw.medigo.auction.domain.model;

import edu.escuelaing.arsw.medigo.auction.infrastructure.config.AuctionTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auction Domain - Pruebas unitarias de reglas de negocio")
class AuctionDomainTest {

    @Test
    void isEditable_onlyWhenScheduled() {
        Auction scheduled = Auction.builder().status(Auction.AuctionStatus.SCHEDULED).build();
        Auction active = Auction.builder().status(Auction.AuctionStatus.ACTIVE).build();
        
        assertThat(scheduled.isEditable()).isTrue();
        assertThat(active.isEditable()).isFalse();
    }

    @Test
    void isAcceptingBids_onlyWhenActiveAndBeforeEnd() {
        LocalDateTime future = AuctionTime.now().plusHours(1);
        LocalDateTime past = AuctionTime.now().minusHours(1);
        
        Auction activeFuture = Auction.builder().status(Auction.AuctionStatus.ACTIVE).endTime(future).build();
        Auction activePast = Auction.builder().status(Auction.AuctionStatus.ACTIVE).endTime(past).build();
        Auction closed = Auction.builder().status(Auction.AuctionStatus.CLOSED).endTime(future).build();
        
        assertThat(activeFuture.isAcceptingBids()).isTrue();
        assertThat(activePast.isAcceptingBids()).isFalse();
        assertThat(closed.isAcceptingBids()).isFalse();
    }

    @Test
    void shouldCloseByMaxPrice() {
        Auction auction = Auction.builder()
            .closureType(Auction.ClosureType.MAX_PRICE)
            .maxPrice(BigDecimal.valueOf(10000))
            .build();
            
        assertThat(auction.shouldCloseByMaxPrice(BigDecimal.valueOf(10000))).isTrue();
        assertThat(auction.shouldCloseByMaxPrice(BigDecimal.valueOf(11000))).isTrue();
        assertThat(auction.shouldCloseByMaxPrice(BigDecimal.valueOf(9999))).isFalse();
        
        Auction fixed = Auction.builder().closureType(Auction.ClosureType.FIXED_TIME).build();
        assertThat(fixed.shouldCloseByMaxPrice(BigDecimal.valueOf(20000))).isFalse();
    }

    @Test
    void shouldCloseByInactivity() {
        LocalDateTime now = AuctionTime.now();
        Auction auction = Auction.builder()
            .closureType(Auction.ClosureType.INACTIVITY)
            .inactivityMinutes(10)
            .lastBidAt(now.minusMinutes(11))
            .build();
            
        assertThat(auction.shouldCloseByInactivity()).isTrue();
        
        auction = auction.toBuilder().lastBidAt(now.minusMinutes(5)).build();
        assertThat(auction.shouldCloseByInactivity()).isFalse();
        
        // Test with startTime if lastBidAt is null
        auction = auction.toBuilder().lastBidAt(null).startTime(now.minusMinutes(11)).build();
        assertThat(auction.shouldCloseByInactivity()).isTrue();
        
        // Test non-inactivity type
        auction = auction.toBuilder().closureType(Auction.ClosureType.FIXED_TIME).build();
        assertThat(auction.shouldCloseByInactivity()).isFalse();
    }
    
    @Test
    void testBidModel() {
        Bid bid = Bid.builder()
            .id(1L)
            .auctionId(1L)
            .userId(1L)
            .userName("User")
            .amount(BigDecimal.valueOf(5000))
            .placedAt(AuctionTime.now())
            .build();
            
        assertThat(bid.getId()).isEqualTo(1L);
        assertThat(bid.getUserName()).isEqualTo("User");
    }
}
