package edu.escuelaing.arsw.medigo.catalog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Catalog Domain - Pruebas unitarias")
class CatalogDomainTest {

    @Test
    void testMedicationModel() {
        Medication medication = Medication.builder()
            .id(1L)
            .name("Acetaminofen")
            .description("Para el dolor")
            .unit("Tableta")
            .price(BigDecimal.valueOf(500))
            .build();
            
        assertThat(medication.getId()).isEqualTo(1L);
        assertThat(medication.getName()).isEqualTo("Acetaminofen");
        assertThat(medication.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void testBranchStockModel() {
        BranchStock stock = BranchStock.builder()
            .branchId(1L)
            .medicationId(10L)
            .quantity(100)
            .build();
            
        assertThat(stock.getBranchId()).isEqualTo(1L);
        assertThat(stock.getQuantity()).isEqualTo(100);
    }
}
