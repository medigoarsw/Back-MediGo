package edu.escuelaing.arsw.medigo.catalog.domain.dto;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BranchWithMedicationsTest {

    @Test
    void testBranchWithMedicationsBuilderAndGettersSetters() {
        StockWithMedicationInfo stock = StockWithMedicationInfo.builder()
                .medicationId(10L)
                .medicationName("Med X")
                .quantity(100)
                .build();

        BranchWithMedications branchWithMedications = BranchWithMedications.builder()
                .branchId(1L)
                .branchName("Sede Central")
                .address("Avenida 1")
                .latitude(4.0)
                .longitude(-74.0)
                .medications(List.of(stock))
                .build();

        assertEquals(1L, branchWithMedications.getBranchId());
        assertEquals("Sede Central", branchWithMedications.getBranchName());
        assertEquals("Avenida 1", branchWithMedications.getAddress());
        assertEquals(4.0, branchWithMedications.getLatitude());
        assertEquals(-74.0, branchWithMedications.getLongitude());
        assertNotNull(branchWithMedications.getMedications());
        assertEquals(1, branchWithMedications.getMedications().size());
        assertEquals(10L, branchWithMedications.getMedications().get(0).getMedicationId());

        branchWithMedications.setBranchId(2L);
        assertEquals(2L, branchWithMedications.getBranchId());

        branchWithMedications.setBranchName("Sede Sur");
        assertEquals("Sede Sur", branchWithMedications.getBranchName());

        branchWithMedications.setAddress("Avenida 2");
        assertEquals("Avenida 2", branchWithMedications.getAddress());

        branchWithMedications.setLatitude(5.0);
        assertEquals(5.0, branchWithMedications.getLatitude());

        branchWithMedications.setLongitude(-75.0);
        assertEquals(-75.0, branchWithMedications.getLongitude());

        StockWithMedicationInfo stock2 = new StockWithMedicationInfo();
        branchWithMedications.setMedications(List.of(stock, stock2));
        assertEquals(2, branchWithMedications.getMedications().size());
    }
}
