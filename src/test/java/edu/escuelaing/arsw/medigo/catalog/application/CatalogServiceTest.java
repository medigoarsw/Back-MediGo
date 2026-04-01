package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el servicio CatalogService
 */
@DisplayName("CatalogService - Casos de uso")
class CatalogServiceTest {

    @Mock
    private MedicationRepositoryPort medicationRepository;

    private CatalogService catalogService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        catalogService = new CatalogService(medicationRepository);
    }

    // ======================== BUSCAR MEDICAMENTOS ========================

    @Test
    @DisplayName("Debería buscar medicamentos por nombre exitosamente")
    void testSearchByNameSuccess() {
        // Arrange
        String searchTerm = "paracetamol";
        List<Medication> medications = List.of(
                Medication.builder().id(1L).name("Paracetamol 500mg").unit("tableta").build(),
                Medication.builder().id(2L).name("Paracetamol 1000mg").unit("tableta").build()
        );

        when(medicationRepository.findByNameContaining(searchTerm))
                .thenReturn(medications);

        // Act
        List<Medication> result = catalogService.searchByName(searchTerm);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Paracetamol 500mg", result.get(0).getName());
        verify(medicationRepository).findByNameContaining(searchTerm);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el término de búsqueda está vacío")
    void testSearchByNameEmptyTerm() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> catalogService.searchByName(""));
        assertThrows(BusinessException.class, () -> catalogService.searchByName(null));
        verify(medicationRepository, never()).findByNameContaining(anyString());
    }

    @Test
    @DisplayName("Debería retornar lista vacía si no hay resultados")
    void testSearchByNameNoResults() {
        // Arrange
        String searchTerm = "medicamento_inexistente";
        when(medicationRepository.findByNameContaining(searchTerm))
                .thenReturn(List.of());

        // Act
        List<Medication> result = catalogService.searchByName(searchTerm);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== OBTENER STOCK POR SUCURSAL ========================

    @Test
    @DisplayName("Debería obtener stock de una sucursal exitosamente")
    void testGetStockByBranchSuccess() {
        // Arrange
        Long branchId = 5L;
        List<BranchStock> stocks = List.of(
                BranchStock.builder().branchId(5L).medicationId(1L).quantity(35).build(),
                BranchStock.builder().branchId(5L).medicationId(2L).quantity(0).build()
        );

        when(medicationRepository.findStockByBranch(branchId))
                .thenReturn(stocks);

        // Act
        List<BranchStock> result = catalogService.getStockByBranch(branchId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(35, result.get(0).getQuantity());
        verify(medicationRepository).findStockByBranch(branchId);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el ID de sucursal es inválido")
    void testGetStockByBranchInvalidId() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> catalogService.getStockByBranch(null));
        assertThrows(BusinessException.class, () -> catalogService.getStockByBranch(-1L));
        assertThrows(BusinessException.class, () -> catalogService.getStockByBranch(0L));
    }

    // ======================== CREAR MEDICAMENTO ========================

    @Test
    @DisplayName("Debería crear medicamento exitosamente")
    void testCreateMedicationSuccess() {
        // Arrange
        Medication medication = Medication.builder()
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .build();

        Medication savedMedication = Medication.builder()
                .id(10L)
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .build();

        when(medicationRepository.save(any(Medication.class)))
                .thenReturn(savedMedication);

        // Act
        Medication result = catalogService.createMedication(medication, 1L, 100);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Aspirina", result.getName());
        verify(medicationRepository).save(any(Medication.class));
        verify(medicationRepository).updateStock(1L, 10L, 100);
    }

    @Test
    @DisplayName("Debería lanzar excepción si el medicamento es nulo")
    void testCreateMedicationNullData() {
        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.createMedication(null, 1L, 100));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el nombre es vacío")
    void testCreateMedicationEmptyName() {
        // Arrange
        Medication medication = Medication.builder()
                .name("")
                .unit("tableta")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.createMedication(medication, 1L, 100));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el ID de sucursal es inválido")
    void testCreateMedicationInvalidBranch() {
        // Arrange
        Medication medication = Medication.builder()
                .name("Medicamento")
                .unit("tableta")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.createMedication(medication, null, 100));
        assertThrows(BusinessException.class, () ->
                catalogService.createMedication(medication, 0L, 100));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el stock inicial es negativo")
    void testCreateMedicationNegativeStock() {
        // Arrange
        Medication medication = Medication.builder()
                .name("Medicamento")
                .unit("tableta")
                .build();

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.createMedication(medication, 1L, -10));
    }

    // ======================== ACTUALIZAR STOCK ========================

    @Test
    @DisplayName("Debería actualizar stock exitosamente")
    void testUpdateStockSuccess() {
        // Arrange
        Long branchId = 5L;
        Long medicationId = 1L;
        int newQuantity = 50;

        when(medicationRepository.findById(medicationId))
                .thenReturn(Optional.of(
                        Medication.builder().id(1L).name("Medicamento").build()
                ));

        // Act
        catalogService.updateStock(branchId, medicationId, newQuantity);

        // Assert
        verify(medicationRepository).findById(medicationId);
        verify(medicationRepository).updateStock(branchId, medicationId, newQuantity);
    }

    @Test
    @DisplayName("Debería lanzar excepción si medicamento no existe")
    void testUpdateStockMedicationNotFound() {
        // Arrange
        when(medicationRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                catalogService.updateStock(5L, 999L, 50));
    }

    @Test
    @DisplayName("Debería lanzar excepción si la cantidad es negativa")
    void testUpdateStockNegativeQuantity() {
        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.updateStock(5L, 1L, -10));
    }

    @Test
    @DisplayName("Debería lanzar excepción si el ID de medicamento es inválido")
    void testUpdateStockInvalidMedicationId() {
        // Act & Assert
        assertThrows(BusinessException.class, () ->
                catalogService.updateStock(5L, null, 50));
        assertThrows(BusinessException.class, () ->
                catalogService.updateStock(5L, 0L, 50));
    }

    @Test
    @DisplayName("Debería permitir actualizar stock a cero")
    void testUpdateStockToZero() {
        // Arrange
        when(medicationRepository.findById(1L))
                .thenReturn(Optional.of(
                        Medication.builder().id(1L).name("Medicamento").build()
                ));

        // Act
        catalogService.updateStock(5L, 1L, 0);

        // Assert
        verify(medicationRepository).updateStock(5L, 1L, 0);
    }
}
