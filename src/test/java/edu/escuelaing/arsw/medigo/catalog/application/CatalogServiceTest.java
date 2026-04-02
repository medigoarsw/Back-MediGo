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
    @DisplayName("Debería crear medicamento exitosamente (método antiguo)")
    void testCreateMedicationSuccess() {
        // Este test verifica el método antiguo createMedication(Medication, Long, int)
        // Aunque mantiene compatibilidad hacia atrás con UpdateStockUseCase
        Medication medication = Medication.builder()
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .price(java.math.BigDecimal.valueOf(3500))
                .build();

        Medication savedMedication = Medication.builder()
                .id(10L)
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .price(java.math.BigDecimal.valueOf(3500))
                .build();

        when(medicationRepository.save(any(Medication.class)))
                .thenReturn(savedMedication);

        // Act
        Medication result = catalogService.createMedication(medication, 1L, 100);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Aspirina", result.getName());
        assertEquals(java.math.BigDecimal.valueOf(3500), result.getPrice());
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

    // ======================== HU-07: CREAR MEDICAMENTO ========================

    @Test
    @DisplayName("HU-07 Escenario 1: Crear medicamento exitosamente")
    void testCreateMedicationSuccessfully() {
        // Given: el administrador está autenticado e ingresa datos válidos
        String name = "Paracetamol 500mg";
        String description = "Analgésico y antipirético";
        String unit = "Tabletas";
        java.math.BigDecimal price = java.math.BigDecimal.valueOf(5000);
        Long branchId = 1L;
        Integer initialStock = 100;

        when(medicationRepository.save(any(Medication.class)))
                .thenAnswer(invocation -> {
                    Medication med = invocation.getArgument(0);
                    return Medication.builder()
                            .id(10L)
                            .name(med.getName())
                            .description(med.getDescription())
                            .unit(med.getUnit())
                            .price(med.getPrice())  // Incluir precio en el mock
                            .build();
                });

        // When: presiona el botón "Guardar"
        Medication result = catalogService.createMedication(
                name, description, unit, price, branchId, initialStock);

        // Then: el medicamento se guarda en el catálogo
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(unit, result.getUnit());
        assertEquals(price, result.getPrice());

        // Verify: se actualiza el stock en la sucursal
        verify(medicationRepository).save(any(Medication.class));
        verify(medicationRepository).updateStock(branchId, result.getId(), initialStock);
    }

    @Test
    @DisplayName("HU-07 Escenario 2: Intentar crear medicamento con nombre vacío")
    void testCreateMedicationWithEmptyName() {
        // Given: el administrador intenta crear un medicamento sin nombre
        String name = "";
        String description = "Descripción";
        String unit = "Tabletas";
        java.math.BigDecimal price = java.math.BigDecimal.valueOf(5000);
        Long branchId = 1L;
        Integer initialStock = 100;

        // When & Then: lanza excepción por nombre vacío
        BusinessException exception = assertThrows(BusinessException.class, () ->
                catalogService.createMedication(name, description, unit, price, branchId, initialStock)
        );

        assertEquals("El nombre es obligatorio", exception.getMessage());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    @DisplayName("HU-07 Escenario 3: Intentar crear medicamento con precio inválido (0 o negativo)")
    void testCreateMedicationWithInvalidPrice() {
        // Given: el administrador intenta crear un medicamento con precio 0
        String name = "Paracetamol 500mg";
        String description = "Descripción";
        String unit = "Tabletas";
        java.math.BigDecimal priceZero = java.math.BigDecimal.ZERO;
        java.math.BigDecimal priceNegative = java.math.BigDecimal.valueOf(-100);
        Long branchId = 1L;
        Integer initialStock = 100;

        // When & Then: lanza excepción por precio inválido (0)
        BusinessException exceptionZero = assertThrows(BusinessException.class, () ->
                catalogService.createMedication(name, description, unit, priceZero, branchId, initialStock)
        );
        assertEquals("El precio debe ser mayor a 0", exceptionZero.getMessage());

        // When & Then: lanza excepción por precio negativo
        BusinessException exceptionNegative = assertThrows(BusinessException.class, () ->
                catalogService.createMedication(name, description, unit, priceNegative, branchId, initialStock)
        );
        assertEquals("El precio debe ser mayor a 0", exceptionNegative.getMessage());

        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    @DisplayName("HU-07 Escenario 4: Medicamento creado visible en búsqueda de clientes")
    void testCreatedMedicationVisibleInSearch() {
        // Given: el administrador acaba de crear un medicamento "Aspirina 500mg"
        String name = "Aspirina 500mg";
        String description = "Antiinflamatorio";
        String unit = "Tabletas";
        java.math.BigDecimal price = java.math.BigDecimal.valueOf(3500);
        Long branchId = 1L;
        Integer initialStock = 50;

        Medication createdMedication = Medication.builder()
                .id(11L)
                .name(name)
                .description(description)
                .unit(unit)
                .price(price)
                .build();

        when(medicationRepository.save(any(Medication.class)))
                .thenReturn(createdMedication);

        // Crear el medicamento
        Medication result = catalogService.createMedication(
                name, description, unit, price, branchId, initialStock);

        // When: un cliente accede a la plataforma y busca "Aspirina"
        when(medicationRepository.findByNameContaining("Aspirina"))
                .thenReturn(List.of(createdMedication));

        List<Medication> searchResults = catalogService.searchByName("Aspirina");

        // Then: el medicamento aparece en el catálogo general
        assertNotNull(searchResults);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(m -> m.getName().contains("Aspirina")));
        assertEquals(name, searchResults.get(0).getName());
        assertEquals(price, searchResults.get(0).getPrice());

        verify(medicationRepository).save(any(Medication.class));
        verify(medicationRepository).updateStock(branchId, result.getId(), initialStock);
    }
}
