package edu.escuelaing.arsw.medigo.auction.application;

import edu.escuelaing.arsw.medigo.auction.domain.exception.*;
import edu.escuelaing.arsw.medigo.auction.domain.model.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import edu.escuelaing.arsw.medigo.auction.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService implements
        CreateAuctionUseCase,
        UpdateAuctionUseCase,
        PlaceBidUseCase,
        CloseAuctionUseCase,
        QueryAuctionUseCase,
        JoinAuctionUseCase {

    private final AuctionRepositoryPort     auctionRepository;
    private final BidLockPort               bidLock;
    private final AuctionEventPublisherPort eventPublisher;
    private final AuctionParticipantPort    participantPort;
    private final AuctionOrderPort          auctionOrderPort;
    private final AuctionCatalogPort        auctionCatalogPort;

    // ── HU-15: Crear subasta ──────────────────────────────────────

    @Override
    @Transactional
    public Auction createAuction(CreateAuctionCommand cmd) {
        validateAuctionDates(cmd.startTime(), cmd.endTime());
        validateBasePrice(cmd.basePrice());

        if (auctionRepository.existsActiveOrScheduledForMedication(cmd.medicationId())) {
            throw new AuctionAlreadyExistsException(cmd.medicationId());
        }

        Auction auction = Auction.builder()
                .medicationId(cmd.medicationId())
                .branchId(cmd.branchId())
                .basePrice(cmd.basePrice())
                .startTime(cmd.startTime())
                .endTime(cmd.endTime())
                .status(Auction.AuctionStatus.SCHEDULED)
                .closureType(cmd.closureType() != null
                        ? cmd.closureType()
                        : Auction.ClosureType.FIXED_TIME)
                .maxPrice(cmd.maxPrice())
                .inactivityMinutes(cmd.inactivityMinutes())
                .build();

        return auctionRepository.save(auction);
    }

    // ── HU-16: Editar subasta ─────────────────────────────────────

    @Override
    @Transactional
    public Auction updateAuction(Long auctionId, UpdateAuctionCommand cmd) {
        Auction existing = findOrThrow(auctionId);

        if (!existing.isEditable()) {
            throw new AuctionNotEditableException(existing.getStatus().name());
        }

        validateAuctionDates(cmd.startTime(), cmd.endTime());
        validateBasePrice(cmd.basePrice());

        Auction updated = existing.toBuilder()
                .basePrice(cmd.basePrice())
                .startTime(cmd.startTime())
                .endTime(cmd.endTime())
                .build();

        return auctionRepository.save(updated);
    }

    // ── HU-17: Consultar subasta ──────────────────────────────────

    @Override
    public Auction getAuctionById(Long id) {
        return findOrThrow(id);
    }

    @Override
    public List<Auction> getActiveAuctions() {
        return auctionRepository.findActiveAuctions();
    }

    @Override
    public List<QueryAuctionUseCase.AuctionWithPrice> getActiveAuctionsWithCurrentPrice() {
        return auctionRepository.findActiveAuctions()
                .stream()
                .map(auction -> {
                    BigDecimal currentPrice = auctionRepository.findHighestBid(auction.getId())
                            .map(Bid::getAmount)
                            .orElse(null);
                    return new QueryAuctionUseCase.AuctionWithPrice(auction, currentPrice);
                })
                .toList();
    }

    @Override
    public QueryAuctionUseCase.WinnerView getAuctionWinner(Long auctionId) {
        Auction auction = findOrThrow(auctionId);

        if (auction.getWinnerId() == null) {
            return new QueryAuctionUseCase.WinnerView(auctionId, null, null, null);
        }

        return auctionRepository.findHighestBid(auctionId)
                .map(bid -> new QueryAuctionUseCase.WinnerView(
                        auctionId, bid.getUserId(), bid.getUserName(), bid.getAmount()))
                .orElse(new QueryAuctionUseCase.WinnerView(auctionId, auction.getWinnerId(), null, null));
    }

            @Override
            public WonAuctionsPageView getWonAuctionsByAffiliate(Long affiliateId, int page, int size) {
            AuctionRepositoryPort.WonAuctionsPage wonPage =
                auctionRepository.findWonAuctionsByWinnerId(affiliateId, page, size);

            List<WonAuctionView> content = wonPage.content().stream()
                .map(record -> {
                    Auction auction = record.auction();
                    Bid winningBid = record.winningBid();

                    String medicationName = auctionCatalogPort.getMedicationInfo(auction.getMedicationId())
                        .map(AuctionCatalogPort.MedicationInfo::name)
                        .orElse(null);

                    return new WonAuctionView(
                        auction.getId(),
                        medicationName,
                        "Lote #" + auction.getId() + " - Sede " + auction.getBranchId(),
                        auction.getBranchId(),
                        winningBid != null ? winningBid.getAmount() : auction.getBasePrice(),
                        winningBid != null
                            ? winningBid.getPlacedAt()
                            : (auction.getLastBidAt() != null ? auction.getLastBidAt() : auction.getEndTime()),
                        "WON",
                        auction.getClosureType() != null ? auction.getClosureType().name() : null
                    );
                })
                .toList();

            return new WonAuctionsPageView(
                content,
                wonPage.page(),
                wonPage.size(),
                wonPage.totalElements(),
                wonPage.totalPages()
            );
            }

    @Override
    public List<Bid> getBidHistory(Long auctionId) {
        return auctionRepository.findBidsByAuction(auctionId);
    }

    @Override
    public AuctionDetailView getAuctionDetail(Long id) {
        Auction auction = findOrThrow(id);

        String medicationName = null;
        String medicationUnit = null;
        AuctionCatalogPort.MedicationInfo medInfo =
                auctionCatalogPort.getMedicationInfo(auction.getMedicationId()).orElse(null);
        if (medInfo != null) {
            medicationName = medInfo.name();
            medicationUnit = medInfo.unit();
        }

        Duration remaining = null;
        if (auction.getStatus() == Auction.AuctionStatus.ACTIVE
                || auction.getStatus() == Auction.AuctionStatus.SCHEDULED) {
            Duration d = Duration.between(LocalDateTime.now(), auction.getEndTime());
            remaining = d.isNegative() ? Duration.ZERO : d;
        }

        String winnerName = null;
        BigDecimal currentPrice = null;
        var highestBid = auctionRepository.findHighestBid(id);
        if (highestBid.isPresent()) {
            currentPrice = highestBid.get().getAmount();
            if (auction.getWinnerId() != null) {
                winnerName = highestBid.get().getUserName();
            }
        }

        return new AuctionDetailView(auction, medicationName, medicationUnit, remaining, winnerName, currentPrice);
    }

    // ── HU-18: Unirse a subasta ───────────────────────────────────

    @Override
    public void joinAuction(Long auctionId, Long userId) {
        Auction auction = findOrThrow(auctionId);

        if (auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            throw new AuctionClosedException();
        }

        participantPort.addParticipant(auctionId, userId);
    }

    // ── HU-19 + HU-20: Realizar puja con concurrencia ────────────
    /**
     * Mecanismo de concurrencia:
     *
     * 1. Se genera un lockValue unico (UUID) para identificar al dueno del lock.
     * 2. Redis SETNX (SET if Not eXists) con TTL 5s garantiza que SOLO UN hilo
     *    procese una puja a la vez para la misma subasta.
     * 3. Si no se obtiene el lock -> BidLockNotAcquiredException (el cliente reintenta).
     * 4. Dentro del lock: validaciones de negocio + persist en PostgreSQL.
     * 5. El lock se libera en el bloque finally (incluso si hay error).
     * 6. Solo si todo fue exitoso se publica el evento WebSocket.
     *
     * Aislamiento de transaccion SERIALIZABLE para la lectura del monto maximo
     * actual, evitando que dos pujas del mismo monto pasen las validaciones
     * en ventana de tiempo muy corta.
     */
    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Bid placeBid(Long auctionId, Long userId, String userName, BigDecimal amount) {
        Auction auction = findOrThrow(auctionId);

        if (!auction.isAcceptingBids()) {
            throw new AuctionClosedException();
        }

        // HU-19: validar que el usuario se haya unido previamente
        if (!participantPort.isParticipant(auctionId, userId)) {
            throw new UserNotJoinedException(userId, auctionId);
        }

        String lockValue = UUID.randomUUID().toString();
        boolean lockAcquired = false;

        try {
            lockAcquired = bidLock.acquireLock(auctionId, lockValue);

            if (!lockAcquired) {
                throw new BidLockNotAcquiredException();
            }

            // Validar monto contra la puja mas alta actual
            BigDecimal currentMax = auctionRepository
                    .findHighestBid(auctionId)
                    .map(Bid::getAmount)
                    .orElse(auction.getBasePrice());

            if (amount.compareTo(currentMax) <= 0) {
                throw new InvalidBidException(
                    "La puja debe ser mayor al monto actual de $" + currentMax);
            }

            // Persistir puja
            Bid bid = Bid.builder()
                    .auctionId(auctionId)
                    .userId(userId)
                    .userName(userName)
                    .amount(amount)
                    .placedAt(LocalDateTime.now())
                    .build();

            Bid saved = auctionRepository.saveBid(bid);
            auctionRepository.updateLastBidAt(auctionId, saved.getPlacedAt());

            // Publicar evento en tiempo real a todos los participantes
            AuctionEvent event = AuctionEvent.builder()
                    .type(AuctionEvent.EventType.BID_PLACED)
                    .auctionId(auctionId)
                    .currentAmount(amount)
                    .leaderName(userName)
                    .leaderId(userId)
                    .timestamp(saved.getPlacedAt())
                    .message(userName + " pujó $" + amount)
                    .build();

            eventPublisher.publish(auctionId, event);

            // Verificar si la puja activa cierre por monto maximo (HU-21 escenario 3)
            if (auction.shouldCloseByMaxPrice(amount)) {
                log.info("Subasta {} alcanzó monto máximo. Cerrando.", auctionId);
                closeAndAdjudicate(auctionId);
            }

            return saved;

        } finally {
            if (lockAcquired) {
                bidLock.releaseLock(auctionId, lockValue);
            }
        }
    }

    // ── HU-21: Cierre automatico (scheduler) ─────────────────────

    /**
     * Corre cada 30 segundos.
     * Evalua tres tipos de cierre:
     *   1. FIXED_TIME  : endTime <= now
     *   2. INACTIVITY  : now - lastBidAt > inactivityMinutes
     *   3. MAX_PRICE   : manejado en placeBid directamente
     */
    @Scheduled(fixedRate = 30_000)
    public void autoCloseExpiredAuctions() {
        // Activar subastas SCHEDULED cuyo startTime ya paso
        auctionRepository.findScheduledReadyToStart().forEach(a -> {
            log.info("Activando subasta {}", a.getId());
            auctionRepository.updateStatus(a.getId(), Auction.AuctionStatus.ACTIVE);
            eventPublisher.publish(a.getId(), AuctionEvent.builder()
                    .type(AuctionEvent.EventType.AUCTION_STARTED)
                    .auctionId(a.getId())
                    .timestamp(LocalDateTime.now())
                    .message("La subasta ha comenzado")
                    .build());
        });

        // Cerrar subastas expiradas por tiempo fijo
        auctionRepository.findExpiredActiveAuctions().forEach(a -> {
            log.info("Cerrando subasta {} por tiempo", a.getId());
            closeAndAdjudicate(a.getId());
        });

        // Cerrar subastas por inactividad
        auctionRepository.findActiveAuctions().stream()
                .filter(Auction::shouldCloseByInactivity)
                .forEach(a -> {
                    log.info("Cerrando subasta {} por inactividad", a.getId());
                    closeAndAdjudicate(a.getId());
                });
    }

    // ── HU-22: Cierre y adjudicación ─────────────────────────────

    @Override
    @Transactional
    public Auction closeAuction(Long auctionId) {
        auctionRepository.updateStatus(auctionId, Auction.AuctionStatus.CLOSED);
        return findOrThrow(auctionId);
    }

    @Override
    @Transactional
    public void adjudicateWinner(Long auctionId) {
        auctionRepository.findHighestBid(auctionId).ifPresent(winningBid -> {
            auctionRepository.setWinner(auctionId, winningBid.getUserId());

            AuctionEvent event = AuctionEvent.builder()
                    .type(AuctionEvent.EventType.WINNER_ADJUDICATED)
                    .auctionId(auctionId)
                    .currentAmount(winningBid.getAmount())
                    .leaderName(winningBid.getUserName())
                    .leaderId(winningBid.getUserId())
                    .timestamp(LocalDateTime.now())
                    .message("Ganador: " + winningBid.getUserName()
                            + " con $" + winningBid.getAmount())
                    .build();

            eventPublisher.publish(auctionId, event);
            log.info("Subasta {} adjudicada a userId={} por ${}",
                    auctionId, winningBid.getUserId(), winningBid.getAmount());
        });
    }

    // ── HU-22 + Segundo lugar: Cron de 24h ───────────────────────

    /**
     * Corre cada hora. Busca órdenes PENDING_PAYMENT creadas hace más de 24h
     * y ejecuta la lógica de re-adjudicación al segundo lugar.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkExpiredPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<AuctionOrderPort.ExpiredAuctionOrder> expired =
                auctionOrderPort.findExpiredPendingOrders(cutoff);

        for (AuctionOrderPort.ExpiredAuctionOrder expiredOrder : expired) {
            log.info("Pago expirado - orderId={}, auctionId={}",
                    expiredOrder.orderId(), expiredOrder.auctionId());

            auctionOrderPort.cancelOrder(expiredOrder.orderId());

            Auction auction = findOrThrow(expiredOrder.auctionId());

            auctionRepository.findSecondHighestBid(expiredOrder.auctionId(), expiredOrder.winnerId())
                    .ifPresentOrElse(
                        secondBid -> readjudicateToSecond(auction, secondBid),
                        () -> {
                            auctionCatalogPort.releaseStock(
                                    auction.getBranchId(), auction.getMedicationId());
                            log.info("Sin segundo lugar. Stock liberado para medicamento={}",
                                    auction.getMedicationId());
                        }
                    );
        }
    }

    // ── Helpers privados ──────────────────────────────────────────

    private void closeAndAdjudicate(Long auctionId) {
        closeAuction(auctionId);
        adjudicateWinner(auctionId);

        // HU-22: generar orden y reservar stock para el ganador
        Bid winningBid = auctionRepository.findHighestBid(auctionId).orElse(null);
        if (winningBid != null) {
            Auction auction = findOrThrow(auctionId);
            Long orderId = auctionOrderPort.createAuctionOrder(
                    auctionId, winningBid.getUserId(), auction.getMedicationId(),
                    winningBid.getAmount(), auction.getBranchId());
            log.info("Orden {} creada para ganador userId={}", orderId, winningBid.getUserId());

            try {
                auctionCatalogPort.reserveStock(auction.getBranchId(), auction.getMedicationId());
            } catch (Exception e) {
                log.warn("No se pudo reservar stock para subasta {}: {}", auctionId, e.getMessage());
            }
        }

        // Incluir datos del ganador en el evento AUCTION_CLOSED para que el front
        // pueda mostrar al ganador en el mismo mensaje de cierre.
        AuctionEvent.AuctionEventBuilder closedEvent = AuctionEvent.builder()
                .type(AuctionEvent.EventType.AUCTION_CLOSED)
                .auctionId(auctionId)
                .timestamp(LocalDateTime.now());

        if (winningBid != null) {
            closedEvent
                    .currentAmount(winningBid.getAmount())
                    .leaderName(winningBid.getUserName())
                    .leaderId(winningBid.getUserId())
                    .message("La subasta finalizó. Ganador: " + winningBid.getUserName()
                            + " con $" + winningBid.getAmount());
        } else {
            closedEvent.message("La subasta ha finalizado sin pujas");
        }

        eventPublisher.publish(auctionId, closedEvent.build());
    }

    private void readjudicateToSecond(Auction auction, Bid secondBid) {
        Long newOrderId = auctionOrderPort.createAuctionOrder(
                auction.getId(), secondBid.getUserId(), auction.getMedicationId(),
                secondBid.getAmount(), auction.getBranchId());

        auctionRepository.setWinner(auction.getId(), secondBid.getUserId());

        eventPublisher.publish(auction.getId(), AuctionEvent.builder()
                .type(AuctionEvent.EventType.WINNER_ADJUDICATED)
                .auctionId(auction.getId())
                .currentAmount(secondBid.getAmount())
                .leaderName(secondBid.getUserName())
                .leaderId(secondBid.getUserId())
                .timestamp(LocalDateTime.now())
                .message("Re-adjudicación: " + secondBid.getUserName()
                        + " con $" + secondBid.getAmount())
                .build());

        log.info("Re-adjudicado a userId={}, nuevaOrden={}", secondBid.getUserId(), newOrderId);
    }

    private Auction findOrThrow(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));
    }

    private void validateAuctionDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new InvalidAuctionDatesException("Las fechas de inicio y fin son requeridas");
        }
        if (!start.isBefore(end)) {
            throw new InvalidAuctionDatesException(
                "La fecha de inicio debe ser anterior a la fecha de fin");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new InvalidAuctionDatesException(
                "La subasta no puede comenzar en el pasado");
        }
    }

    private void validateBasePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAuctionDatesException("El precio base debe ser mayor a 0");
        }
    }
}
