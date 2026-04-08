package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "InventoryListResponse", description = "Listado paginado de inventario para administración")
public class InventoryListResponse {

    private List<InventoryMedicationItemResponse> items;
    private PaginationMeta meta;
}
