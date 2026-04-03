error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/adapter/in/MedicationController.java:java/util/List#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/adapter/in/MedicationController.java
empty definition using pc, found symbol in pc: java/util/List#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 1579
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO%20OFICIAL/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/catalog/infrastructure/adapter/in/MedicationController.java
text:
```scala
package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateStockUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.CreateMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.*;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out.MedicationJpaRepository;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.@@List;

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
    private final CreateMedicationUseCase createUseCase;  // HU-07: Inyectar CreateMedicationUseCase
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
     * Obtener medicamentos disponibles por sucursal
     */
    @GetMapping("/branch/{branchId}/medications")
    @Operation(
        summary = "Obtener medicamentos por sucursal",
        description = "Obtiene la lista completa de medicamentos disponibles en una sucursal específica con su información enriquecida (nombre, descripción, unidad)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Medicamentos obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "array",
                    example = """
                        [
                          {
                            "medicationId": 1,
                            "medicationName": "Ibuprofeno 400mg",
                            "description": "Antiinflamatorio y analgésico",
                            "unit": "Caja x30",
                            "quantity": 150
                          },
                          {
                            "medicationId": 2,
                            "medicationName": "Paracetamol 500mg",
                            "description": "Analgésico y antipirético",
                            "unit": "Tableta",
                            "quantity": 75
                          }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de sucursal inválido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Sucursal no encontrada"
        )
    })
    public ResponseEntity<List<MedicationBranchStockResponse>> getMedicationsByBranch(
            @Parameter(
                name = "branchId",
                description = "ID único de la sucursal",
                example = "1",
                required = true
            )
            @PathVariable Long branchId) {

        log.info("Obteniendo medicamentos para sucursal: {}", branchId);

        List<StockWithMedicationInfo> stocks = medicationRepository.findMedicationsByBranch(branchId);
        List<MedicationBranchStockResponse> responses = stocks.stream()
                .map(stock -> MedicationBranchStockResponse.builder()
                        .medicationId(stock.getMedicationId())
                        .medicationName(stock.getMedicationName())
                        .description(stock.getDescription())
                        .unit(stock.getMedicationUnit())
                        .quantity(stock.getQuantity())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * Obtener medicamentos agrupados por sucursal
     */
    @GetMapping("/branches")
    @Operation(
        summary = "Obtener medicamentos por todas las sucursales",
        description = "Obtiene una vista consolidada de todas las sucursales con los medicamentos disponibles en cada una. Útil para dashboards y reportes"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Sucursales y medicamentos obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "array",
                    example = """
                        [
                          {
                            "branchId": 1,
                            "branchName": "Sucursal Centro",
                            "address": "Calle 10 # 5-20",
                            "latitude": 4.72160,
                            "longitude": -74.04499,
                            "medications": [
                              {
                                "medicationId": 1,
                                "medicationName": "Ibuprofeno 400mg",
                                "description": "Antiinflamatorio y analgésico",
                                "unit": "Caja x30",
                                "quantity": 150
                              }
                            ]
                          },
                          {
                            "branchId": 2,
                            "branchName": "Sucursal Norte",
                            "address": "Carrera 7 # 120-45",
                            "latitude": 4.75250,
                            "longitude": -74.02300,
                            "medications": [
                              {
                                "medicationId": 1,
                                "medicationName": "Ibuprofeno 400mg",
                                "description": "Antiinflamatorio y analgésico",
                                "unit": "Caja x30",
                                "quantity": 200
                              }
                            ]
                          }
                        ]
                        """
                )
            )
        )
    })
    public ResponseEntity<List<BranchMedicationsResponse>> getMedicationsByBranches() {
        log.info("Obteniendo medicamentos agrupados por todas las sucursales");

        List<edu.escuelaing.arsw.medigo.catalog.domain.dto.BranchWithMedications> branches;
        try {
            branches = searchUseCase.getAllMedicationsByBranches();
        } catch (Exception e) {
            log.warn("Error al obtener sucursales con medicamentos: {}", e.getMessage());
            branches = List.of();
        }

        List<BranchMedicationsResponse> responses = branches.stream()
                .map(branch -> BranchMedicationsResponse.builder()
                        .branchId(branch.getBranchId())
                        .branchName(branch.getBranchName())
                        .address(branch.getAddress())
                        .latitude(branch.getLatitude())
                        .longitude(branch.getLongitude())
                        .medications(branch.getMedications().stream()
                                .map(stock -> MedicationBranchStockResponse.builder()
                                        .medicationId(stock.getMedicationId())
                                        .medicationName(stock.getMedicationName())
                                        .description(stock.getDescription())
                                        .unit(stock.getMedicationUnit())
                                        .quantity(stock.getQuantity())
                                        .build())
                                .toList())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    /**
     * HU-07: Crear un nuevo medicamento con stock inicial
     * Solo los administradores pueden crear medicamentos
     */
    @PostMapping
     // HU-07: Validar que sea admin
    @Operation(
        summary = "Crear medicamento (Admin)",
        description = "Crea un nuevo medicamento en el catálogo con stock inicial en una sucursal. Solo administradores."
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
                          "name": "Paracetamol 500mg",
                          "description": "Analgésico y antipirético",
                          "unit": "tableta",
                          "price": 5000.00
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos: nombre vacío, presentación vacía, precio 0 o negativo, stock 0 o negativo"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No autorizado: solo administradores pueden crear medicamentos"
        )
    })
    public ResponseEntity<MedicationResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del medicamento a crear",
                required = true
            )
            @Valid @RequestBody CreateMedicationRequest request) {

        log.info("HU-07: Creando medicamento: {} en sucursal: {} con stock inicial: {}",
                request.getName(), request.getBranchId(), request.getInitialStock());

        Medication created = createUseCase.createMedication(
                request.getName(),
                request.getDescription(),
                request.getUnit(),
                request.getPrice(),
                request.getBranchId(),
                request.getInitialStock()
        );

        log.info("HU-07: Medicamento creado exitosamente con ID: {}", created.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toMedicationResponse(created));
    }

    /**
     * HU-08: Administrador edita disponibilidad (stock)
     * Solo los administradores pueden actualizar el stock
     */
    @PutMapping("/{medicationId}/branch/{branchId}/stock")
    @Operation(
        summary = "Actualizar disponibilidad (Admin)",
        description = "Actualiza la cantidad disponible de un medicamento en una sucursal específica. Solo administradores. Crea el registro si no existe."
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
            responseCode = "403",
            description = "No autorizado: solo administradores pueden actualizar stock"
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
     * Obtener disponibilidad de un medicamento en una sucursal específica (HU-04)
     */
    @GetMapping("/{medicationId}/availability/branch/{branchId}")
    @Operation(
        summary = "Ver disponibilidad de medicamento en sucursal",
        description = "Obtiene la disponibilidad de un medicamento específico en una sucursal en tiempo real. " +
                     "Muestra cantidad disponible, estado (disponible/no disponible) y datos de la sucursal."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Disponibilidad obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BranchAvailabilityResponse.class),
                examples = {
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Medicamento disponible",
                        value = """
                            {
                              "branchId": 1,
                              "branchName": "Sucursal Centro",
                              "address": "Calle 10 # 5-20",
                              "latitude": 4.72160,
                              "longitude": -74.04499,
                              "quantity": 5,
                              "isAvailable": true,
                              "availabilityStatus": "Disponible"
                            }
                            """
                    ),
                    @io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "Medicamento no disponible",
                        value = """
                            {
                              "branchId": 2,
                              "branchName": "Sucursal Norte",
                              "address": "Carrera 7 # 120-45",
                              "latitude": 4.75250,
                              "longitude": -74.02300,
                              "quantity": 0,
                              "isAvailable": false,
                              "availabilityStatus": "No disponible"
                            }
                            """
                    )
                }
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de medicamento o sucursal inválido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medicamento no encontrado"
        )
    })
    public ResponseEntity<BranchAvailabilityResponse> getAvailabilityInBranch(
            @Parameter(
                name = "medicationId",
                description = "ID del medicamento",
                required = true
            )
            @PathVariable Long medicationId,
            @Parameter(
                name = "branchId",
                description = "ID de la sucursal",
                required = true
            )
            @PathVariable Long branchId) {

        log.info("Obteniendo disponibilidad del medicamento {} en sucursal {}", medicationId, branchId);

        // Obtener la disponibilidad
        BranchStock stock = searchUseCase.getAvailabilityByMedicationBranch(medicationId, branchId);
        
        BranchAvailabilityResponse response = BranchAvailabilityResponse.builder()
                .branchId(branchId)
                .quantity(stock.getQuantity())
                .isAvailable(stock.getQuantity() > 0)
                .availabilityStatus(stock.getQuantity() > 0 ? "Disponible" : "No disponible")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener disponibilidad de un medicamento en todas las sucursales (HU-04)
     */
    @GetMapping("/{medicationId}/availability/branches")
    @Operation(
        summary = "Ver disponibilidad de medicamento en todas las sucursales",
        description = "Obtiene la disponibilidad de un medicamento en todas las sucursales en tiempo real. " +
                     "Útil para que el cliente vea dónde está disponible y dónde no. Incluye total disponible y número de sucursales con stock."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Disponibilidad obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MedicationAvailabilityResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID de medicamento inválido"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Medicamento no encontrado"
        )
    })
    public ResponseEntity<MedicationAvailabilityResponse> getAvailabilityInAllBranches(
            @Parameter(
                name = "medicationId",
                description = "ID del medicamento",
                required = true
            )
            @PathVariable Long medicationId) {

        log.info("Obteniendo disponibilidad del medicamento {} en todas las sucursales", medicationId);

        // Obtener medicamento
        Medication medication = searchUseCase.findById(medicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con ID: " + medicationId));

        // Obtener disponibilidad en todas las sucursales
        List<BranchStock> stocks = searchUseCase.getAvailabilityByMedicationAllBranches(medicationId);

        // Convertir a responses
        List<BranchAvailabilityResponse> availabilityByBranch = stocks.stream()
                .map(stock -> BranchAvailabilityResponse.builder()
                        .branchId(stock.getBranchId())
                        .quantity(stock.getQuantity())
                        .isAvailable(stock.getQuantity() > 0)
                        .availabilityStatus(stock.getQuantity() > 0 ? "Disponible" : "No disponible")
                        .build())
                .toList();

        // Calcular totales
        int totalAvailable = stocks.stream().mapToInt(BranchStock::getQuantity).sum();
        int branchesWithStock = (int) stocks.stream()
                .filter(s -> s.getQuantity() > 0)
                .count();

        MedicationAvailabilityResponse response = MedicationAvailabilityResponse.builder()
                .medicationId(medication.getId())
                .medicationName(medication.getName())
                .description(medication.getDescription())
                .unit(medication.getUnit())
                .availabilityByBranch(availabilityByBranch)
                .totalAvailable(totalAvailable)
                .branchesWithStock(branchesWithStock)
                .build();

        return ResponseEntity.ok(response);
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
                .price(medication.getPrice())  // HU-07: Incluir precio en respuesta
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

```


#### Short summary: 

empty definition using pc, found symbol in pc: java/util/List#