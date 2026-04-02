package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final CreateAuctionUseCase  createAuctionUseCase;
    private final UpdateAuctionUseCase  updateAuctionUseCase;
    private final PlaceBidUseCase       placeBidUseCase;
    private final QueryAuctionUseCase   queryAuctionUseCase;
    private final JoinAuctionUseCase    joinAuctionUseCase;

    // HU-15: Crear subasta (solo ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse create(@Valid @RequestBody CreateAuctionRequest req) {
        Auction auction = createAuctionUseCase.createAuction(
            new CreateAuctionUseCase.CreateAuctionCommand(
                req.medicationId(), req.branchId(), req.basePrice(),
                req.startTime(), req.endTime(),
                req.closureType() != null
                    ? Auction.ClosureType.valueOf(req.closureType())
                    : Auction.ClosureType.FIXED_TIME,
                req.maxPrice(), req.inactivityMinutes()
            )
        );
        return AuctionResponse.from(auction);
    }

    // HU-16: Editar subasta programada (solo ADMIN)
    @PutMapping("/{id}")
    public AuctionResponse update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateAuctionRequest req) {
        Auction auction = updateAuctionUseCase.updateAuction(id,
            new UpdateAuctionUseCase.UpdateAuctionCommand(
                req.basePrice(), req.startTime(), req.endTime()
            )
        );
        return AuctionResponse.from(auction);
    }

    // HU-17: Ver detalle de subasta (enriquecido con catálogo, tiempo restante y ganador)
    @GetMapping("/{id}")
    public AuctionResponse getById(@PathVariable Long id) {
        return AuctionResponse.fromDetail(queryAuctionUseCase.getAuctionDetail(id));
    }

    // HU-17: Listar subastas activas
    @GetMapping("/active")
    public List<AuctionResponse> getActive() {
        return queryAuctionUseCase.getActiveAuctions()
                .stream().map(AuctionResponse::from).toList();
    }

    // HU-17: Historial de pujas de una subasta
    @GetMapping("/{id}/bids")
    public List<BidResponse> getBids(@PathVariable Long id) {
        return queryAuctionUseCase.getBidHistory(id)
                .stream().map(BidResponse::from).toList();
    }

    // HU-18: Unirse a subasta
    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void join(@PathVariable Long id, @RequestParam Long userId) {
        joinAuctionUseCase.joinAuction(id, userId);
    }

    // HU-19: Realizar puja (con concurrencia Redis SETNX)
    @PostMapping("/{id}/bids")
    @ResponseStatus(HttpStatus.CREATED)
    public BidResponse placeBid(@PathVariable Long id,
                                 @Valid @RequestBody PlaceBidRequest req) {
        return BidResponse.from(
            placeBidUseCase.placeBid(id, req.userId(), req.userName(), req.amount())
        );
    }
}
