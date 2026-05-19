package edu.escuelaing.arsw.medigo.catalog.domain.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StockWithMedicationInfoTest {

    @Test
    void testStockWithMedicationInfoBuilderAndGetters() {
        StockWithMedicationInfo stock = StockWithMedicationInfo.builder()
                .medicationId(10L)
                .medicationName("Med X")
                .description("Desc X")
                .medicationUnit("Caja")
                .branchId(1L)
                .quantity(100)
                .build();

        assertEquals(10L, stock.getMedicationId());
        assertEquals("Med X", stock.getMedicationName());
        assertEquals("Desc X", stock.getDescription());
        assertEquals("Caja", stock.getMedicationUnit());
        assertEquals(1L, stock.getBranchId());
        assertEquals(100, stock.getQuantity());
        assertTrue(stock.isAvailable());

        StockWithMedicationInfo stockZero = StockWithMedicationInfo.builder()
                .quantity(0)
                .build();
        assertFalse(stockZero.isAvailable());

        StockWithMedicationInfo stockNull = StockWithMedicationInfo.builder()
                .quantity(null)
                .build();
        assertFalse(stockNull.isAvailable());

        StockWithMedicationInfo stockNegative = StockWithMedicationInfo.builder()
                .quantity(-5)
                .build();
        assertFalse(stockNegative.isAvailable());
    }
}
