error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/in/AuctionController.java:java/lang/Math#min().
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/in/AuctionController.java
empty definition using pc, found symbol in pc: java/lang/Math#min().
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 3943
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/auction/infrastructure/adapter/in/AuctionController.java
text:
```scala
package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto.*;
import edu.escuelaing.arsw.medigo.shared.infrastructure.security.AuthenticatedUserResolver;
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
    private final AuthenticatedUserResolver authenticatedUserResolver;

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
        return queryAuctionUseCase.getActiveAuctionsWithCurrentPrice()
                .stream().map(AuctionResponse::fromActive).toList();
    }

    // HU-17: Historial de pujas de una subasta
    @GetMapping("/{id}/bids")
    public List<BidResponse> getBids(@PathVariable Long id) {
        return queryAuctionUseCase.getBidHistory(id)
                .stream().map(BidResponse::from).toList();
    }

    // HU-22: Consultar ganador de una subasta cerrada
    @GetMapping("/{id}/winner")
    public ResponseEntity<WinnerResponse> getWinner(@PathVariable Long id) {
        QueryAuctionUseCase.WinnerView view = queryAuctionUseCase.getAuctionWinner(id);
        if (view.winnerId() == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new WinnerResponse(
                view.auctionId(), view.winnerId(), view.winnerName(), view.winningAmount()));
    }

    @GetMapping("/won")
    public WonAuctionsPageResponse getWonAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.@@min(size, 100));

        Long affiliateId = authenticatedUserResolver.getAuthenticatedUserId();
        QueryAuctionUseCase.WonAuctionsPageView wonPage =
                queryAuctionUseCase.getWonAuctionsByAffiliate(affiliateId, safePage, safeSize);

        return WonAuctionsPageResponse.from(wonPage, "/api/auctions/won");
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/lang/Math#min().