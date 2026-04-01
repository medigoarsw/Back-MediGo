package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateStockUseCase;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.*;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out.MedicationJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar medicamentos y su disponibilidad
 */
@RestController
@RequestMapping("/api/medications")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Gestión del catálogo de medicamentos")
@Slf4j
public class MedicationController {

    private final SearchMedicationUseCase searchUseCase;
    private final UpdateStockUseCase updateUseCase;
    private final MedicationJpaRepository medicationRepository;

    /**
     * Buscar medicamentos por nombre (búsqueda parcial)
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar medicamentos por nombre",
        description = "Realiza una búsqueda parcial (LIKE) de medicamentos en el catálogo, insensible a mayúsculas/minúsculas"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Medicamentos encontrados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "array",
                    example = """
                        [
                          {
                            "id": 1,
                            "name": "Paracetamol 500mg",
                            "description": "Analgésico y antipirético",
                            "unit": "tableta"
                          },
                          {
                            "id": 2,
                            "name": "Paracetamol 1000mg",
                            "description": "Analgésico y antipirético potente",
                            "unit": "tableta"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Término de búsqueda inválido"
        )
    })
    public ResponseEntity<List<MedicationResponse>> search(
            @Parameter(
                name = "name",
                description = "Término de búsqueda (búsqueda parcial)",
                example = "paracetamol",
                required = true
            )
            @RequestParam String name) {

        log.info("Búsqueda de medicamentos por nombre: {}", name);

        List<Medication> medications = searchUseCase.searchByName(name);
        List<MedicationResponse> responses = medications.stream()
                .map(this::toMedicationResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Obtener disponibilidad de medicamentos en una sucursal
     */
    @GetMapping("/branch/{branchId}/stock")
    @Operation(
        summary = "Obtener disponibilidad en una sucursal",
        description = "Obtiene la lista de medicamentos disponibles en una sucursal específica con sus cantidades de stock"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Stock obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "array",
                    example = """
                        [
                          {
                            "medicationId": 1,
                            "medicationName": "Paracetamol 500mg",
                            "branchId": 5,
                            "quantity": 35,
                            "isAvailable": true,
                            "unit": "tableta"
                          },
                          {
                            "medicationId": 2,
                            "medicationName": "Ibuprofeno 200mg",
                            "branchId": 5,
                            "quantity": 0,
                            "isAvailable": false,
                            "unit": "tableta"
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de sucursal inválido"
        )
    })
    public ResponseEntity<List<StockResponse>> getStock(
            @Parameter(
                name = "branchId",
                description = "ID único de la sucursal",
                example = "5",
                required = true
            )
            @PathVariable Long branchId) {

        log.info("Obteniendo stock para sucursal: {}", branchId);

        List<StockWithMedicationInfo> stocks = medicationRepository.findStockByBranchWithMedicationInfo(branchId);
        List<StockResponse> responses = stocks.stream()
                .map(this::toStockResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Crear un nuevo medicamento con stock inicial
     */
    @PostMapping
    @Operation(
        summary = "Crear medicamento",
        description = "Crea un nuevo medicamento en el catálogo con stock inicial en una sucursal"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Medicamento creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    example = """
                        {
                          "id": 10,
                          "name": "Aspirina 100mg",
                          "description": "Antiinflamatorio y anticoagulante",
                          "unit": "tableta"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o incompletos"
        )
    })
    public ResponseEntity<MedicationResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del medicamento a crear",
                required = true
            )
            @Valid @RequestBody CreateMedicationRequest request) {

        log.info("Creando medicamento: {} en sucursal: {} con stock inicial: {}",
                request.getName(), request.getBranchId(), request.getInitialStock());

        Medication medication = Medication.builder()
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .build();

        Medication created = updateUseCase.createMedication(
                medication,
                request.getBranchId(),
                request.getInitialStock()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toMedicationResponse(created));
    }

    /**
     * Actualizar disponibilidad de medicamento en una sucursal
     */
    @PutMapping("/{medicationId}/branch/{branchId}/stock")
    @Operation(
        summary = "Actualizar disponibilidad",
        description = "Actualiza la cantidad disponible de un medicamento en una sucursal específica. Crea el registro si no existe."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Stock actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos o cantidad negativa"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medicamento no encontrado"
        )
    })
    public ResponseEntity<Void> updateStock(
            @Parameter(
                name = "medicationId",
                description = "ID del medicamento",
                example = "1",
                required = true
            )
            @PathVariable Long medicationId,

            @Parameter(
                name = "branchId",
                description = "ID de la sucursal",
                example = "5",
                required = true
            )
            @PathVariable Long branchId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nueva cantidad de stock",
                required = true
            )
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("Actualizando stock - Medicamento: {}, Sucursal: {}, Nueva cantidad: {}",
                medicationId, branchId, request.getQuantity());

        updateUseCase.updateStock(branchId, medicationId, request.getQuantity());

        return ResponseEntity.noContent().build();
    }

    /**
     * Mapea un modelo de Medicamento a su DTO de respuesta
     */
    private MedicationResponse toMedicationResponse(Medication medication) {
        return MedicationResponse.builder()
                .id(medication.getId())
                .name(medication.getName())
                .description(medication.getDescription())
                .unit(medication.getUnit())
                .build();
    }

    /**
     * Mapea un DTO de dominio de StockWithMedicationInfo a su DTO de respuesta REST
     */
    private StockResponse toStockResponse(StockWithMedicationInfo stock) {
        return StockResponse.builder()
                .medicationId(stock.getMedicationId())
                .medicationName(stock.getMedicationName())
                .branchId(stock.getBranchId())
                .quantity(stock.getQuantity())
                .isAvailable(stock.isAvailable())
                .unit(stock.getMedicationUnit())
                .build();
    }
}
