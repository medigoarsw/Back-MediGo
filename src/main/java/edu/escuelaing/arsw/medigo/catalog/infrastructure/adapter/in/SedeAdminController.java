package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.application.SedeAdminService;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.PaginationMeta;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeListData;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeResponse;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeUpdateRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import edu.escuelaing.arsw.medigo.shared.infrastructure.api.ApiEnvelope;
import edu.escuelaing.arsw.medigo.shared.infrastructure.api.TraceIdResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sedes Admin", description = "CRUD de sedes para administradores")
@SecurityRequirement(name = "bearerAuth")
public class SedeAdminController {

    private final SedeAdminService sedeService;

    @GetMapping
    @Operation(summary = "Listar sedes", description = "Lista paginada de sedes activas con filtro de texto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado exitoso"),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos", content = @Content(schema = @Schema(implementation = ApiEnvelope.class))),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<ApiEnvelope<SedeListData>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String q,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceHeader) {

        String traceId = TraceIdResolver.resolve(traceHeader);
        Page<BranchEntity> result = sedeService.list(page, limit, q);

        List<SedeResponse> items = result.getContent().stream().map(this::toResponse).toList();
        PaginationMeta meta = PaginationMeta.builder()
                .page(page)
                .limit(limit)
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .build();

        SedeListData data = SedeListData.builder()
                .items(items)
                .pagination(meta)
                .build();

        log.info("traceId={} listado sedes page={} limit={} q={}", traceId, page, limit, q);
        return ResponseEntity.ok(envelope(true, "Sedes obtenidas exitosamente", data, traceId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sede por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle de sede"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<ApiEnvelope<SedeResponse>> getById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceHeader) {

        String traceId = TraceIdResolver.resolve(traceHeader);
        SedeResponse data = toResponse(sedeService.getById(id));
        log.info("traceId={} consulta sede id={}", traceId, id);
        return ResponseEntity.ok(envelope(true, "Sede obtenida exitosamente", data, traceId));
    }

    @PostMapping
    @Operation(summary = "Crear sede")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sede creada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<ApiEnvelope<SedeResponse>> create(
            @Valid @RequestBody SedeRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceHeader) {

        String traceId = TraceIdResolver.resolve(traceHeader);
        SedeResponse data = toResponse(sedeService.create(request));
        log.info("traceId={} sede creada id={}", traceId, data.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(envelope(true, "Sede creada exitosamente", data, traceId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar sede", description = "PUT con semantica parcial para update total o parcial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede actualizada"),
            @ApiResponse(responseCode = "400", description = "Payload invalido"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<ApiEnvelope<SedeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SedeUpdateRequest request,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceHeader) {

        String traceId = TraceIdResolver.resolve(traceHeader);
        SedeResponse data = toResponse(sedeService.update(id, request));
        log.info("traceId={} sede actualizada id={}", traceId, id);
        return ResponseEntity.ok(envelope(true, "Sede actualizada exitosamente", data, traceId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar sede", description = "Soft delete de sede")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede desactivada"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada"),
            @ApiResponse(responseCode = "403", description = "Solo ADMIN")
    })
    public ResponseEntity<ApiEnvelope<Void>> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceHeader) {

        String traceId = TraceIdResolver.resolve(traceHeader);
        sedeService.softDelete(id);
        log.info("traceId={} sede desactivada id={}", traceId, id);
        return ResponseEntity.ok(envelope(true, "Sede desactivada exitosamente", null, traceId));
    }

    private SedeResponse toResponse(BranchEntity entity) {
        return SedeResponse.builder()
                .id(entity.getId())
                .nombre(entity.getName())
                .direccion(entity.getAddress())
                .especialidad(entity.getSpecialty())
                .telefono(entity.getPhone())
                .capacidad(entity.getCapacity())
                .build();
    }

    private <T> ApiEnvelope<T> envelope(boolean success, String message, T data, String traceId) {
        return ApiEnvelope.<T>builder()
                .success(success)
                .message(message)
                .data(data)
                .traceId(traceId)
                .apiVersion("v1")
                .timestamp(Instant.now().toString())
                .build();
    }
}
