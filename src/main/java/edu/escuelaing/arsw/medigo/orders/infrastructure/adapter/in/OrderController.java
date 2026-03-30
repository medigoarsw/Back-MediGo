package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.in;
import edu.escuelaing.arsw.medigo.orders.domain.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/orders") @RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    @PostMapping public ResponseEntity<?> create(@RequestBody Object req) { return ResponseEntity.ok().build(); }
    @PostMapping("/{id}/confirm") public ResponseEntity<?> confirm(@PathVariable Long id) { return ResponseEntity.ok().build(); }
    @GetMapping("/my-orders") public ResponseEntity<?> myOrders() { return ResponseEntity.ok().build(); }
}