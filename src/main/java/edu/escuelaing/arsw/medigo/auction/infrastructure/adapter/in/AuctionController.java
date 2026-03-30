package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auctions") @RequiredArgsConstructor
public class AuctionController {
    private final CreateAuctionUseCase createAuctionUseCase;
    private final PlaceBidUseCase placeBidUseCase;
    @PostMapping public ResponseEntity<?> create(@RequestBody Object req) { return ResponseEntity.ok().build(); }
    @PutMapping("/{id}") public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Object req) { return ResponseEntity.ok().build(); }
    @GetMapping("/active") public ResponseEntity<?> getActive() { return ResponseEntity.ok().build(); }
    @GetMapping("/{id}") public ResponseEntity<?> getById(@PathVariable Long id) { return ResponseEntity.ok().build(); }
    @PostMapping("/{id}/bids") public ResponseEntity<?> placeBid(@PathVariable Long id, @RequestBody Object req) { return ResponseEntity.ok().build(); }
}