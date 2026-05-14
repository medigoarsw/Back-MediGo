package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BranchMedicationsResponseTest {

    @Test
    void testBranchMedicationsResponseBuilderAndGettersSetters() {
        MedicationBranchStockResponse stock = MedicationBranchStockResponse.builder()
                .medicationId(1L)
                .medicationName("Med A")
                .description("Desc A")
                .unit("Tablet")
                .quantity(10)
                .build();

        BranchMedicationsResponse response = BranchMedicationsResponse.builder()
                .branchId(1L)
                .branchName("Sede Norte")
                .address("Calle 10")
                .latitude(4.5)
                .longitude(-74.0)
                .medications(List.of(stock))
                .build();

        assertEquals(1L, response.getBranchId());
        assertEquals("Sede Norte", response.getBranchName());
        assertEquals("Calle 10", response.getAddress());
        assertEquals(4.5, response.getLatitude());
        assertEquals(-74.0, response.getLongitude());
        assertNotNull(response.getMedications());
        assertEquals(1, response.getMedications().size());

        MedicationBranchStockResponse stockResponse = response.getMedications().get(0);
        assertEquals(1L, stockResponse.getMedicationId());
        assertEquals("Med A", stockResponse.getMedicationName());
        assertEquals("Desc A", stockResponse.getDescription());
        assertEquals(10, stockResponse.getQuantity());
        assertEquals("Tablet", stockResponse.getUnit());

        // Setters
        response.setBranchId(2L);
        assertEquals(2L, response.getBranchId());

        response.setBranchName("Sede Sur");
        assertEquals("Sede Sur", response.getBranchName());

        response.setAddress("Calle 20");
        assertEquals("Calle 20", response.getAddress());

        response.setLatitude(5.0);
        assertEquals(5.0, response.getLatitude());

        response.setLongitude(-75.0);
        assertEquals(-75.0, response.getLongitude());

        MedicationBranchStockResponse stock2 = new MedicationBranchStockResponse();
        stock2.setMedicationId(2L);
        stock2.setMedicationName("Med B");
        stock2.setDescription("Desc B");
        stock2.setQuantity(20);
        stock2.setUnit("Bottle");

        response.setMedications(List.of(stock, stock2));
        assertEquals(2, response.getMedications().size());
    }
}
