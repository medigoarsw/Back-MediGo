package edu.escuelaing.arsw.medigo.auction.application;

import edu.escuelaing.arsw.medigo.auction.application.AuctionService;
import edu.escuelaing.arsw.medigo.auction.domain.exception.*;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.CreateAuctionUseCase.CreateAuctionCommand;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.UpdateAuctionUseCase.UpdateAuctionCommand;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.*;
import edu.escuelaing.arsw.medigo.auction.infrastructure.config.AuctionTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.QueryAuctionUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService - Pruebas de concurrencia y reglas de negocio")
class AuctionServiceTest {

    @Mock AuctionRepositoryPort     auctionRepository;
    @Mock BidLockPort               bidLock;
    @Mock AuctionEventPublisherPort eventPublisher;
    @Mock AuctionParticipantPort    participantPort;
    @Mock AuctionOrderPort          auctionOrderPort;
    @Mock AuctionCatalogPort        auctionCatalogPort;

    @InjectMocks AuctionService sut;

    private static final LocalDateTime FUTURE_START = AuctionTime.now().plusHours(1);
    private static final LocalDateTime FUTURE_END   = AuctionTime.now().plusHours(3);

    @Test
    void createAuction_success() {
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.of(
            new AuctionCatalogPort.MedicationInfo(1L, "Med", "Unit")
        ));
        when(auctionRepository.existsActiveOrScheduledForMedication(1L)).thenReturn(false);
        when(auctionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );

        Auction result = sut.createAuction(cmd);
        assertThat(result.getStatus()).isEqualTo(Auction.AuctionStatus.SCHEDULED);
    }

    @Test
    void createAuction_invalidDates_startAfterEnd() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), FUTURE_END, FUTURE_START, Auction.ClosureType.FIXED_TIME, null, null
        );
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(InvalidAuctionDatesException.class);
    }

    @Test
    void updateAuction_success() {
        Auction auction = buildAuction(Auction.AuctionStatus.SCHEDULED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Auction updated = sut.updateAuction(1L, new UpdateAuctionCommand(BigDecimal.valueOf(6000), FUTURE_START, FUTURE_END));
        assertThat(updated.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    void updateAuction_activeAuction_throws() {
        Auction active = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(active));
        assertThatThrownBy(() -> sut.updateAuction(1L, new UpdateAuctionCommand(BigDecimal.valueOf(6000), FUTURE_START, FUTURE_END)))
            .isInstanceOf(AuctionNotEditableException.class);
    }

    @Test
    void getAuctionById() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        assertThat(sut.getAuctionById(1L)).isEqualTo(auction);
    }

    @Test
    void getActiveAuctions() {
        when(auctionRepository.findActiveAuctions()).thenReturn(List.of(buildAuction(Auction.AuctionStatus.ACTIVE)));
        assertThat(sut.getActiveAuctions()).hasSize(1);
    }

    @Test
    void getActiveAuctionsWithCurrentPrice() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findActiveAuctions()).thenReturn(List.of(auction));
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(6000))));
        
        List<QueryAuctionUseCase.AuctionWithPrice> res = sut.getActiveAuctionsWithCurrentPrice();
        assertThat(res).hasSize(1);
        assertThat(res.get(0).currentPrice()).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    void getAuctionWinner_hasWinner() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED).toBuilder().winnerId(2L).build();
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(6000))));

        QueryAuctionUseCase.WinnerView view = sut.getAuctionWinner(1L);
        assertThat(view.winnerId()).isEqualTo(1L);
    }

    @Test
    void getAuctionWinner_noWinner() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        QueryAuctionUseCase.WinnerView view = sut.getAuctionWinner(1L);
        assertThat(view.winnerId()).isNull();
    }

    @Test
    void getWonAuctionsByAffiliate() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        Bid bid = buildBid(BigDecimal.valueOf(6000));
        AuctionRepositoryPort.WonAuctionRecord record = new AuctionRepositoryPort.WonAuctionRecord(auction, bid);
        AuctionRepositoryPort.WonAuctionsPage page = new AuctionRepositoryPort.WonAuctionsPage(List.of(record), 0, 10, 1, 1);
        
        when(auctionRepository.findWonAuctionsByWinnerId(1L, 0, 10)).thenReturn(page);
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.of(new AuctionCatalogPort.MedicationInfo(1L, "Med", "Unit")));
        
        QueryAuctionUseCase.WonAuctionsPageView view = sut.getWonAuctionsByAffiliate(1L, 0, 10);
        assertThat(view.content()).hasSize(1);
    }

    @Test
    void getBidHistory() {
        when(auctionRepository.findBidsByAuction(1L)).thenReturn(List.of(buildBid(BigDecimal.valueOf(6000))));
        assertThat(sut.getBidHistory(1L)).hasSize(1);
    }

    @Test
    void getAuctionDetail() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.of(new AuctionCatalogPort.MedicationInfo(1L, "Med", "Unit")));
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(6000))));
        
        QueryAuctionUseCase.AuctionDetailView detail = sut.getAuctionDetail(1L);
        assertThat(detail.medicationName()).isEqualTo("Med");
        assertThat(detail.currentPrice()).isEqualByComparingTo(BigDecimal.valueOf(6000));
    }

    @Test
    void joinAuction_success() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        
        sut.joinAuction(1L, 2L);
        verify(participantPort).addParticipant(1L, 2L);
    }

    @Test
    void joinAuction_closed() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        assertThatThrownBy(() -> sut.joinAuction(1L, 2L)).isInstanceOf(AuctionClosedException.class);
    }

    @Test
    void placeBid_validBid_success() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(true);
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(5500))));
        when(auctionRepository.saveBid(any())).thenAnswer(inv -> inv.getArgument(0));

        Bid result = sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000));
        assertThat(result.getAmount()).isEqualByComparingTo("6000");
    }

    @Test
    void autoCloseExpiredAuctions() {
        Auction scheduled = buildAuction(Auction.AuctionStatus.SCHEDULED);
        Auction activeExpired = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findScheduledReadyToStart()).thenReturn(List.of(scheduled));
        when(auctionRepository.findExpiredActiveAuctions()).thenReturn(List.of(activeExpired));
        when(auctionRepository.findActiveAuctions()).thenReturn(List.of());
        
        when(auctionRepository.findById(activeExpired.getId())).thenReturn(Optional.of(activeExpired));

        sut.autoCloseExpiredAuctions();
        
        verify(auctionRepository).updateStatus(scheduled.getId(), Auction.AuctionStatus.ACTIVE);
        verify(auctionRepository).updateStatus(activeExpired.getId(), Auction.AuctionStatus.CLOSED);
    }

    @Test
    void closeAuction() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        Auction result = sut.closeAuction(1L);
        verify(auctionRepository).updateStatus(1L, Auction.AuctionStatus.CLOSED);
    }

    @Test
    void adjudicateWinner() {
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(6000))));
        sut.adjudicateWinner(1L);
        verify(auctionRepository).setWinner(1L, 1L);
    }

    @Test
    void checkExpiredPayments_hasSecondPlace() {
        AuctionOrderPort.ExpiredAuctionOrder order = new AuctionOrderPort.ExpiredAuctionOrder(100L, 1L, 2L);
        when(auctionOrderPort.findExpiredPendingOrders(any())).thenReturn(List.of(order));
        
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        
        Bid secondBid = buildBid(BigDecimal.valueOf(5000));
        when(auctionRepository.findSecondHighestBid(1L, 2L)).thenReturn(Optional.of(secondBid));
        
        sut.checkExpiredPayments();
        
        verify(auctionOrderPort).cancelOrder(100L);
        verify(auctionRepository).setWinner(1L, secondBid.getUserId());
    }

    @Test
    void checkExpiredPayments_noSecondPlace() {
        AuctionOrderPort.ExpiredAuctionOrder order = new AuctionOrderPort.ExpiredAuctionOrder(100L, 1L, 2L);
        when(auctionOrderPort.findExpiredPendingOrders(any())).thenReturn(List.of(order));
        
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        
        when(auctionRepository.findSecondHighestBid(1L, 2L)).thenReturn(Optional.empty());
        
        sut.checkExpiredPayments();
        
        verify(auctionCatalogPort).releaseStock(auction.getBranchId(), auction.getMedicationId());
    }

    private Auction buildAuction(Auction.AuctionStatus status) {
        return Auction.builder()
                .id(1L).medicationId(1L).branchId(1L)
                .basePrice(BigDecimal.valueOf(5000))
                .startTime(AuctionTime.now().minusMinutes(10))
                .endTime(AuctionTime.now().plusHours(2))
                .status(status)
                .closureType(Auction.ClosureType.FIXED_TIME)
                .maxPrice(BigDecimal.valueOf(10000))
                .build();
    }

    private Bid buildBid(BigDecimal amount) {
        return Bid.builder()
                .id(1L).auctionId(1L).userId(1L)
                .userName("Otro").amount(amount)
                .placedAt(AuctionTime.now()).build();
    }

    @Test
    void createAuction_medicationNotFound_throws() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException.class);
    }

    @Test
    void createAuction_auctionExists_throws() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.of(
            new AuctionCatalogPort.MedicationInfo(1L, "Med", "Unit")
        ));
        when(auctionRepository.existsActiveOrScheduledForMedication(1L)).thenReturn(true);
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(AuctionAlreadyExistsException.class);
    }

    @Test
    void createAuction_nullDatesAndPrice_throws() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), null, FUTURE_END, null, null, null
        );
        assertThatThrownBy(() -> sut.createAuction(cmd)).isInstanceOf(InvalidAuctionDatesException.class);
        
        CreateAuctionCommand cmd2 = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), FUTURE_START, null, null, null, null
        );
        assertThatThrownBy(() -> sut.createAuction(cmd2)).isInstanceOf(InvalidAuctionDatesException.class);

        CreateAuctionCommand cmd3 = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000), AuctionTime.now().minusDays(1), FUTURE_END, null, null, null
        );
        assertThatThrownBy(() -> sut.createAuction(cmd3)).isInstanceOf(InvalidAuctionDatesException.class);

        CreateAuctionCommand cmd4 = new CreateAuctionCommand(
            1L, 1L, BigDecimal.ZERO, FUTURE_START, FUTURE_END, null, null, null
        );
        assertThatThrownBy(() -> sut.createAuction(cmd4)).isInstanceOf(InvalidAuctionDatesException.class);
    }

    @Test
    void getWonAuctionsByAffiliate_emptyMedInfoAndBid() {
        Auction auction = buildAuction(Auction.AuctionStatus.CLOSED);
        AuctionRepositoryPort.WonAuctionRecord record = new AuctionRepositoryPort.WonAuctionRecord(auction, null);
        AuctionRepositoryPort.WonAuctionsPage page = new AuctionRepositoryPort.WonAuctionsPage(List.of(record), 0, 10, 1, 1);
        
        when(auctionRepository.findWonAuctionsByWinnerId(1L, 0, 10)).thenReturn(page);
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.empty());
        
        QueryAuctionUseCase.WonAuctionsPageView view = sut.getWonAuctionsByAffiliate(1L, 0, 10);
        assertThat(view.content()).hasSize(1);
        assertThat(view.content().get(0).medicationName()).isNull();
        assertThat(view.content().get(0).finalAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    void getAuctionDetail_noMedInfo_noHighestBid() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.empty());
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.empty());
        
        QueryAuctionUseCase.AuctionDetailView detail = sut.getAuctionDetail(1L);
        assertThat(detail.medicationName()).isNull();
        assertThat(detail.currentPrice()).isNull();
    }

    @Test
    void getAuctionDetail_activeNegativeDuration() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE).toBuilder().endTime(AuctionTime.now().minusHours(1)).build();
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionCatalogPort.getMedicationInfo(1L)).thenReturn(Optional.of(new AuctionCatalogPort.MedicationInfo(1L, "Med", "Unit")));
        
        QueryAuctionUseCase.AuctionDetailView detail = sut.getAuctionDetail(1L);
        assertThat(detail.remainingTime()).isEqualTo(java.time.Duration.ZERO);
    }

    @Test
    void placeBid_exceptions() {
        Auction closed = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(closed));
        assertThatThrownBy(() -> sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(AuctionClosedException.class);

        Auction active = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(2L)).thenReturn(Optional.of(active));
        when(participantPort.isParticipant(2L, 2L)).thenReturn(false);
        assertThatThrownBy(() -> sut.placeBid(2L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(UserNotJoinedException.class);

        when(participantPort.isParticipant(2L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(2L), anyString())).thenReturn(false);
        assertThatThrownBy(() -> sut.placeBid(2L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(BidLockNotAcquiredException.class);

        when(bidLock.acquireLock(eq(2L), anyString())).thenReturn(true);
        when(auctionRepository.findHighestBid(2L)).thenReturn(Optional.of(buildBid(BigDecimal.valueOf(6000))));
        assertThatThrownBy(() -> sut.placeBid(2L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(InvalidBidException.class);
    }

    @Test
    void placeBid_closesByMaxPrice_andReserveStockFails() {
        Auction active = buildAuction(Auction.AuctionStatus.ACTIVE).toBuilder().closureType(Auction.ClosureType.MAX_PRICE).build();
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(active));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(true);
        
        Bid highestBid = buildBid(BigDecimal.valueOf(9000));
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.of(highestBid)); 
        
        Bid newBid = buildBid(BigDecimal.valueOf(10000));
        when(auctionRepository.saveBid(any())).thenReturn(newBid);

        // Reserve stock throws exception to test catch block
        doThrow(new RuntimeException("Stock error")).when(auctionCatalogPort).reserveStock(1L, 1L);

        Bid result = sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(10000));
        assertThat(result.getAmount()).isEqualByComparingTo("10000");
        verify(auctionRepository).updateStatus(1L, Auction.AuctionStatus.CLOSED);
    }

    @Test
    void autoCloseExpiredAuctions_inactivity() {
        Auction active = buildAuction(Auction.AuctionStatus.ACTIVE).toBuilder()
            .closureType(Auction.ClosureType.INACTIVITY)
            .inactivityMinutes(5)
            .lastBidAt(AuctionTime.now().minusMinutes(10))
            .build();
            
        when(auctionRepository.findActiveAuctions()).thenReturn(List.of(active));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(active));
        
        sut.autoCloseExpiredAuctions();
        
        verify(auctionRepository).updateStatus(1L, Auction.AuctionStatus.CLOSED);
    }

    @Test
    void closeAndAdjudicate_noWinningBid() {
        Auction activeExpired = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findExpiredActiveAuctions()).thenReturn(List.of(activeExpired));
        when(auctionRepository.findById(activeExpired.getId())).thenReturn(Optional.of(activeExpired));
        when(auctionRepository.findHighestBid(1L)).thenReturn(Optional.empty());

        sut.autoCloseExpiredAuctions();

        verify(eventPublisher, atLeastOnce()).publish(eq(1L), any(AuctionEvent.class));
    }
}