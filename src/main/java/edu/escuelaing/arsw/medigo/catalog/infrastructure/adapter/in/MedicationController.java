package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/medications") @RequiredArgsConstructor
public class MedicationController {
    private final SearchMedicationUseCase searchUseCase;
    private final UpdateStockUseCase updateUseCase;
    @GetMapping("/search") public ResponseEntity<?> search(@RequestParam String name) { return ResponseEntity.ok().build(); }
    @GetMapping("/branch/{branchId}/stock") public ResponseEntity<?> getStock(@PathVariable Long branchId) { return ResponseEntity.ok().build(); }
    @PostMapping public ResponseEntity<?> create(@RequestBody Object req) { return ResponseEntity.ok().build(); }
    @PutMapping("/{id}/stock") public ResponseEntity<?> updateStock(@PathVariable Long id, @RequestBody Object req) { return ResponseEntity.ok().build(); }
}