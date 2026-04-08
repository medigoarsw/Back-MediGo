package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SedeListData {
    private List<SedeResponse> items;
    private PaginationMeta pagination;
}
