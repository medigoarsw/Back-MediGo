package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.domain.dto.BranchWithMedications;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService - Unit Tests")
class CatalogServiceTest {

    @Mock
    private MedicationRepositoryPort medicationRepository;

    @InjectMocks
    private CatalogService catalogService;

    private Medication mockMedication;
    private BranchStock mockStock;

    @BeforeEach
    void setUp() {
        mockMedication = Medication.builder()
                .id(1L)
                .name("Paracetamol")
                .description("Para el dolor")
                .unit("Caja 10 pastillas")
                .price(new BigDecimal("5000.00"))
                .build();

        mockStock = new BranchStock(10L, 1L, 100);
    }

    @Test
    @DisplayName("searchByName - Success")
    void searchByName_success() {
        when(medicationRepository.findByNameContaining("Para")).thenReturn(List.of(mockMedication));

        List<Medication> results = catalogService.searchByName("Para");

        assertFalse(results.isEmpty());
        assertEquals("Paracetamol", results.get(0).getName());
    }

    @Test
    @DisplayName("searchByName - Empty Name throws BusinessException")
    void searchByName_emptyName_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.searchByName(""));
        assertThrows(BusinessException.class, () -> catalogService.searchByName("  "));
    }

    @Test
    @DisplayName("getStockByBranch - Success")
    void getStockByBranch_success() {
        when(medicationRepository.findStockByBranch(10L)).thenReturn(List.of(mockStock));

        List<BranchStock> results = catalogService.getStockByBranch(10L);

        assertFalse(results.isEmpty());
        assertEquals(100, results.get(0).getQuantity());
    }

    @Test
    @DisplayName("getStockByBranch - Invalid ID throws BusinessException")
    void getStockByBranch_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.getStockByBranch(0L));
    }

    @Test
    @DisplayName("createMedication (Object) - Success")
    void createMedication_object_success() {
        when(medicationRepository.save(any(Medication.class))).thenReturn(mockMedication);

        Medication result = catalogService.createMedication(mockMedication, 10L, 50);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
        verify(medicationRepository).updateStock(10L, 1L, 50);
    }

    @Test
    @DisplayName("createMedication (Object) - Negative Stock throws BusinessException")
    void createMedication_object_negativeStock_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.createMedication(mockMedication, 10L, -5));
    }

    @Test
    @DisplayName("createMedication (Object) - Null branch throws BusinessException")
    void createMedication_object_nullBranch_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.createMedication(mockMedication, null, 50));
    }

    @Test
    @DisplayName("createMedication (Object) - Invalid medication data throws BusinessException")
    void createMedication_object_invalidMedication_throwsException() {
        Medication invalid = Medication.builder().build();
        assertThrows(BusinessException.class, () -> catalogService.createMedication(invalid, 10L, 50));
        assertThrows(BusinessException.class, () -> catalogService.createMedication(null, 10L, 50));
    }

    @Test
    @DisplayName("createMedication (Params) - Success")
    void createMedication_params_success() {
        when(medicationRepository.save(any(Medication.class))).thenReturn(mockMedication);

        Medication result = catalogService.createMedication("Paracetamol", "Para el dolor", "Caja 10 pastillas", new BigDecimal("5000.00"), 10L, 50);

        assertNotNull(result);
        assertEquals("Paracetamol", result.getName());
        verify(medicationRepository).updateStock(10L, 1L, 50);
    }

    @Test
    @DisplayName("createMedication (Params) - Invalid Params throws BusinessException")
    void createMedication_params_invalidParams_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.createMedication("", "Desc", "Unit", new BigDecimal("500"), 10L, 50));
        assertThrows(BusinessException.class, () -> catalogService.createMedication("Name", "Desc", "", new BigDecimal("500"), 10L, 50));
        assertThrows(BusinessException.class, () -> catalogService.createMedication("Name", "Desc", "Unit", new BigDecimal("-5"), 10L, 50));
        assertThrows(BusinessException.class, () -> catalogService.createMedication("Name", "Desc", "Unit", new BigDecimal("500"), 0L, 50));
        assertThrows(BusinessException.class, () -> catalogService.createMedication("Name", "Desc", "Unit", new BigDecimal("500"), 10L, -5));
    }

    @Test
    @DisplayName("updateStock - Success")
    void updateStock_success() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(mockMedication));

        catalogService.updateStock(10L, 1L, 150);

        verify(medicationRepository).updateStock(10L, 1L, 150);
    }

    @Test
    @DisplayName("updateStock - Negative Quantity throws BusinessException")
    void updateStock_negativeQuantity_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.updateStock(10L, 1L, -5));
    }

    @Test
    @DisplayName("updateStock - Medication Not Found throws ResourceNotFoundException")
    void updateStock_notFound_throwsException() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> catalogService.updateStock(10L, 1L, 150));
    }
    
    @Test
    @DisplayName("updateStock - Invalid ID throws Exception")
    void updateStock_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.updateStock(0L, 1L, 150));
        assertThrows(BusinessException.class, () -> catalogService.updateStock(10L, 0L, 150));
    }

    @Test
    @DisplayName("getMedicationsByBranch - Success")
    void getMedicationsByBranch_success() {
        StockWithMedicationInfo info = new StockWithMedicationInfo(1L, "Paracetamol", "Para el dolor", "Caja", 10L, 50);
        when(medicationRepository.findMedicationsByBranch(10L)).thenReturn(List.of(info));

        List<StockWithMedicationInfo> results = catalogService.getMedicationsByBranch(10L);

        assertFalse(results.isEmpty());
        assertEquals(50, results.get(0).getQuantity());
    }

    @Test
    @DisplayName("getMedicationsByBranch - Invalid ID throws Exception")
    void getMedicationsByBranch_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.getMedicationsByBranch(0L));
    }

    @Test
    @DisplayName("getAllMedicationsByBranches - Success")
    void getAllMedicationsByBranches_success() {
        BranchWithMedications branchWithMedications = new BranchWithMedications();
        when(medicationRepository.findAllBranchesWithMedications()).thenReturn(List.of(branchWithMedications));

        List<BranchWithMedications> results = catalogService.getAllMedicationsByBranches();

        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("findById - Success")
    void findById_success() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(mockMedication));

        Optional<Medication> result = catalogService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("findById - Invalid ID throws BusinessException")
    void findById_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.findById(0L));
    }

    @Test
    @DisplayName("getAvailabilityByMedicationBranch - Success")
    void getAvailabilityByMedicationBranch_success() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(mockMedication));
        when(medicationRepository.findStockByMedicationAndBranch(1L, 10L)).thenReturn(mockStock);

        BranchStock result = catalogService.getAvailabilityByMedicationBranch(1L, 10L);

        assertNotNull(result);
        assertEquals(100, result.getQuantity());
    }

    @Test
    @DisplayName("getAvailabilityByMedicationBranch - Missing Stock Returns 0")
    void getAvailabilityByMedicationBranch_missingStock_returnsZero() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(mockMedication));
        when(medicationRepository.findStockByMedicationAndBranch(1L, 10L)).thenReturn(null);

        BranchStock result = catalogService.getAvailabilityByMedicationBranch(1L, 10L);

        assertNotNull(result);
        assertEquals(0, result.getQuantity());
    }
    
    @Test
    @DisplayName("getAvailabilityByMedicationBranch - Invalid ID throws Exception")
    void getAvailabilityByMedicationBranch_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.getAvailabilityByMedicationBranch(0L, 10L));
        assertThrows(BusinessException.class, () -> catalogService.getAvailabilityByMedicationBranch(1L, 0L));
    }

    @Test
    @DisplayName("getAvailabilityByMedicationAllBranches - Success")
    void getAvailabilityByMedicationAllBranches_success() {
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(mockMedication));
        when(medicationRepository.findStockByMedication(1L)).thenReturn(List.of(mockStock));

        List<BranchStock> results = catalogService.getAvailabilityByMedicationAllBranches(1L);

        assertFalse(results.isEmpty());
        assertEquals(10L, results.get(0).getBranchId());
    }
    
    @Test
    @DisplayName("getAvailabilityByMedicationAllBranches - Invalid ID throws Exception")
    void getAvailabilityByMedicationAllBranches_invalidId_throwsException() {
        assertThrows(BusinessException.class, () -> catalogService.getAvailabilityByMedicationAllBranches(0L));
    }
}
