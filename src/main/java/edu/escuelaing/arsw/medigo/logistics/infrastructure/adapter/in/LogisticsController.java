package edu.escuelaing.arsw.medigo.logistics.infrastructure.adapter.in;
import edu.escuelaing.arsw.medigo.logistics.domain.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/logistics") @RequiredArgsConstructor
public class LogisticsController {
    private final UpdateLocationUseCase updateLocationUseCase;
    private final AssignDeliveryUseCase assignDeliveryUseCase;
    @PutMapping("/deliveries/{id}/location") public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Object req) { return ResponseEntity.ok().build(); }
    @PutMapping("/deliveries/{id}/complete") public ResponseEntity<?> complete(@PathVariable Long id) { return ResponseEntity.ok().build(); }
    @PostMapping("/deliveries/assign") public ResponseEntity<?> assign(@RequestBody Object req) { return ResponseEntity.ok().build(); }
}