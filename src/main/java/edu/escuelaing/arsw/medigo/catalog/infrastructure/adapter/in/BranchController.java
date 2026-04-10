package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Branch;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.CreateBranchUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateBranchUseCase;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.BranchResponse;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.CreateBranchRequest;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.BranchRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Gestión de sucursales y centros médicos")
@Slf4j
public class BranchController {

    private final CreateBranchUseCase createUseCase;
    private final UpdateBranchUseCase updateUseCase;
    private final BranchRepositoryPort branchRepositoryPort; // For listing

    @PostMapping
    @Operation(summary = "Crear sucursal (Admin)", description = "Crea un nuevo centro médico con coordenadas.")
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        log.info("Creando sucursal: {}", request.getName());
        Branch branch = createUseCase.createBranch(
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(branch));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sucursal (Admin)", description = "Actualiza los datos de un centro médico existente.")
    public ResponseEntity<BranchResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateBranchRequest request) {
        log.info("Actualizando sucursal ID: {}", id);
        Branch branch = updateUseCase.updateBranch(
                id,
                request.getName(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );
        return ResponseEntity.ok(toResponse(branch));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sucursal (Admin)", description = "Elimina un centro médico.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Eliminando sucursal ID: {}", id);
        updateUseCase.deleteBranch(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Listar sucursales", description = "Retorna todas las sucursales con sus coordenadas.")
    public ResponseEntity<List<BranchResponse>> getAll() {
        List<Branch> branches = branchRepositoryPort.findAll();
        return ResponseEntity.ok(branches.stream().map(this::toResponse).toList());
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .address(branch.getAddress())
                .latitude(branch.getLatitude())
                .longitude(branch.getLongitude())
                .build();
    }
}
