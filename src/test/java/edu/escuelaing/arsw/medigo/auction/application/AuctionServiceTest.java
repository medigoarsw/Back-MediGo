package edu.escuelaing.arsw.medigo.auction.application;
 
import edu.escuelaing.arsw.medigo.auction.domain.exception.*;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.CreateAuctionUseCase.CreateAuctionCommand;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
 
    private static final LocalDateTime FUTURE_START = LocalDateTime.now().plusHours(1);
    private static final LocalDateTime FUTURE_END   = LocalDateTime.now().plusHours(3);
 
    // ── HU-15 ──────────────────────────────────────────────────────
 
    @Test
    @DisplayName("HU-15 E1: Crear subasta exitosamente")
    void createAuction_success() {
        when(auctionRepository.existsActiveOrScheduledForMedication(1L)).thenReturn(false);
        when(auctionRepository.save(any())).thenAnswer(inv -> {
            Auction a = inv.getArgument(0);
            return a.toBuilder().id(10L).build();
        });
 
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000),
            FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );
 
        Auction result = sut.createAuction(cmd);
 
        assertThat(result.getStatus()).isEqualTo(Auction.AuctionStatus.SCHEDULED);
        assertThat(result.getId()).isEqualTo(10L);
    }
 
    @Test
    @DisplayName("HU-15 E2: Fechas invalidas — inicio posterior a fin")
    void createAuction_invalidDates_startAfterEnd() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000),
            FUTURE_END, FUTURE_START,  // invertidas
            Auction.ClosureType.FIXED_TIME, null, null
        );
 
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(InvalidAuctionDatesException.class)
            .hasMessageContaining("anterior a la fecha de fin");
    }
 
    @Test
    @DisplayName("HU-15 E3: Fechas pasadas")
    void createAuction_pastDates() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000),
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1),
            Auction.ClosureType.FIXED_TIME, null, null
        );
 
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(InvalidAuctionDatesException.class)
            .hasMessageContaining("pasado");
    }
 
    @Test
    @DisplayName("HU-15 E4: Precio base invalido")
    void createAuction_invalidPrice() {
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.ZERO,
            FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );
 
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(InvalidAuctionDatesException.class)
            .hasMessageContaining("mayor a 0");
    }
 
    @Test
    @DisplayName("HU-15 E5: Subasta duplicada para mismo medicamento")
    void createAuction_duplicateMedication() {
        when(auctionRepository.existsActiveOrScheduledForMedication(1L)).thenReturn(true);
 
        CreateAuctionCommand cmd = new CreateAuctionCommand(
            1L, 1L, BigDecimal.valueOf(5000),
            FUTURE_START, FUTURE_END,
            Auction.ClosureType.FIXED_TIME, null, null
        );
 
        assertThatThrownBy(() -> sut.createAuction(cmd))
            .isInstanceOf(AuctionAlreadyExistsException.class);
    }
 
    // ── HU-16 ──────────────────────────────────────────────────────
 
    @Test
    @DisplayName("HU-16 E2: No se puede editar subasta activa")
    void updateAuction_activeAuction_throws() {
        Auction active = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(active));
 
        assertThatThrownBy(() -> sut.updateAuction(1L,
            new edu.escuelaing.arsw.medigo.auction.domain.port.in.UpdateAuctionUseCase
                .UpdateAuctionCommand(BigDecimal.valueOf(6000), FUTURE_START, FUTURE_END)))
            .isInstanceOf(AuctionNotEditableException.class)
            .hasMessageContaining("ACTIVE");
    }
 
    // ── HU-19 — Concurrencia ───────────────────────────────────────
 
    @Test
    @DisplayName("HU-19 E1: Puja valida mayor al monto actual")
    void placeBid_validBid_success() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(true);
        when(auctionRepository.findHighestBid(1L))
            .thenReturn(Optional.of(buildBid(BigDecimal.valueOf(5500))));
        when(auctionRepository.saveBid(any())).thenAnswer(inv -> {
            Bid b = inv.getArgument(0);
            return Bid.builder().id(1L).auctionId(b.getAuctionId())
                .userId(b.getUserId()).userName(b.getUserName())
                .amount(b.getAmount()).placedAt(b.getPlacedAt()).build();
        });
 
        Bid result = sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000));
 
        assertThat(result.getAmount()).isEqualByComparingTo("6000");
        verify(eventPublisher).publish(eq(1L), any());
        verify(bidLock).releaseLock(eq(1L), anyString());
    }
 
    @Test
    @DisplayName("HU-19 E2: Puja igual al monto actual — rechazada")
    void placeBid_sameAmount_rejected() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(true);
        when(auctionRepository.findHighestBid(1L))
            .thenReturn(Optional.of(buildBid(BigDecimal.valueOf(5500))));
 
        assertThatThrownBy(() -> sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(5500)))
            .isInstanceOf(InvalidBidException.class)
            .hasMessageContaining("mayor al monto actual");
 
        verify(bidLock).releaseLock(eq(1L), anyString());
    }
 
    @Test
    @DisplayName("HU-19 Concurrencia: lock no disponible — BidLockNotAcquiredException")
    void placeBid_lockNotAvailable_throws() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(false);
 
        assertThatThrownBy(() -> sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(BidLockNotAcquiredException.class);
 
        verify(auctionRepository, never()).saveBid(any());
        verify(eventPublisher, never()).publish(any(), any());
    }
 
    @Test
    @DisplayName("HU-19 Concurrencia: lock se libera aunque haya excepcion")
    void placeBid_lockReleasedOnException() {
        Auction auction = buildAuction(Auction.AuctionStatus.ACTIVE);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(participantPort.isParticipant(1L, 2L)).thenReturn(true);
        when(bidLock.acquireLock(eq(1L), anyString())).thenReturn(true);
        when(auctionRepository.findHighestBid(1L)).thenThrow(new RuntimeException("DB error"));
 
        assertThatThrownBy(() -> sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(RuntimeException.class);
 
        // El lock SIEMPRE debe liberarse en finally
        verify(bidLock).releaseLock(eq(1L), anyString());
    }
 
    @Test
    @DisplayName("HU-19 E5: Puja en subasta cerrada — rechazada")
    void placeBid_closedAuction_throws() {
        Auction closed = buildAuction(Auction.AuctionStatus.CLOSED);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(closed));
 
        assertThatThrownBy(() -> sut.placeBid(1L, 2L, "Juan", BigDecimal.valueOf(6000)))
            .isInstanceOf(AuctionClosedException.class);
    }
 
    // ── Helpers ───────────────────────────────────────────────────
 
    private Auction buildAuction(Auction.AuctionStatus status) {
        return Auction.builder()
                .id(1L).medicationId(1L).branchId(1L)
                .basePrice(BigDecimal.valueOf(5000))
                .startTime(LocalDateTime.now().minusMinutes(10))
                .endTime(LocalDateTime.now().plusHours(2))
                .status(status)
                .closureType(Auction.ClosureType.FIXED_TIME)
                .build();
    }
 
    private Bid buildBid(BigDecimal amount) {
        return Bid.builder()
                .id(1L).auctionId(1L).userId(1L)
                .userName("Otro").amount(amount)
                .placedAt(LocalDateTime.now()).build();
    }
}
 
 