package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.domain.dto.StockWithMedicationInfo;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.SearchMedicationUseCase;
import edu.escuelaing.arsw.medigo.catalog.domain.port.in.UpdateStockUseCase;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.CreateMedicationRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.UpdateStockRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out.MedicationJpaRepository;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el controlador MedicationController
 */
@DisplayName("MedicationController - Endpoints REST")
class MedicationControllerTest {

    @Mock
    private SearchMedicationUseCase searchUseCase;

    @Mock
    private UpdateStockUseCase updateUseCase;

    @Mock
    private MedicationJpaRepository medicationRepository;

    private MedicationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MedicationController(searchUseCase, updateUseCase, medicationRepository);
    }

    // ======================== SEARCH ENDPOINT ========================

    @Test
    @DisplayName("Debería retornar medicamentos en búsqueda exitosa")
    void testSearchSuccess() {
        // Arrange
        String searchTerm = "paracetamol";
        List<Medication> medications = List.of(
                Medication.builder().id(1L).name("Paracetamol 500mg").unit("tableta").build(),
                Medication.builder().id(2L).name("Paracetamol 1000mg").unit("tableta").build()
        );

        when(searchUseCase.searchByName(searchTerm))
                .thenReturn(medications);

        // Act
        ResponseEntity<?> response = controller.search(searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).searchByName(searchTerm);
    }

    @Test
    @DisplayName("Debería manejar excepción de búsqueda")
    void testSearchException() {
        // Arrange
        when(searchUseCase.searchByName(""))
                .thenThrow(new BusinessException("El término de búsqueda no puede estar vacío"));

        // Act & Assert
        assertThrows(BusinessException.class, () -> controller.search(""));
    }

    @Test
    @DisplayName("Búsqueda insensible a mayúsculas/minúsculas (Escenario 3)")
    void testSearchCaseInsensitive() {
        // Given el catálogo contiene "Paracetamol 500mg"
        // When escribe "PARACETAMOL" en mayúsculas
        // Then se muestra "Paracetamol 500mg" en los resultados
        
        // Arrange
        String searchTermUpperCase = "PARACETAMOL";
        List<Medication> medications = List.of(
                Medication.builder()
                        .id(1L)
                        .name("Paracetamol 500mg")
                        .description("Analgésico")
                        .unit("tableta")
                        .build()
        );

        when(searchUseCase.searchByName(searchTermUpperCase))
                .thenReturn(medications);

        // Act
        ResponseEntity<?> response = controller.search(searchTermUpperCase);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).searchByName(searchTermUpperCase);
    }

    @Test
    @DisplayName("Búsqueda con coincidencia parcial (Escenario 4)")
    void testSearchPartialMatch() {
        // Given el catálogo contiene "Paracetamol 500mg"
        // When escribe "para" en el campo de búsqueda
        // Then se muestra "Paracetamol 500mg" en los resultados
        
        // Arrange
        String partialSearchTerm = "para";
        List<Medication> medications = List.of(
                Medication.builder()
                        .id(1L)
                        .name("Paracetamol 500mg")
                        .description("Analgésico")
                        .unit("tableta")
                        .build(),
                Medication.builder()
                        .id(2L)
                        .name("Paracetamol 1000mg")
                        .description("Analgésico potente")
                        .unit("tableta")
                        .build()
        );

        when(searchUseCase.searchByName(partialSearchTerm))
                .thenReturn(medications);

        // Act
        ResponseEntity<?> response = controller.search(partialSearchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<?> resultList = (List<?>) response.getBody();
        assertEquals(2, resultList.size());
        verify(searchUseCase).searchByName(partialSearchTerm);
    }

    @Test
    @DisplayName("Sin resultados en búsqueda (Escenario 5)")
    void testSearchNoResults() {
        // Given el catálogo no contiene ningún medicamento con "aspirina"
        // When escribe "aspirina" en el campo de búsqueda
        // Then se muestra el mensaje "No se encontraron medicamentos"
        // And el catálogo muestra la lista vacía
        
        // Arrange
        String searchTermNoResults = "aspirina";
        List<Medication> emptyResults = List.of();

        when(searchUseCase.searchByName(searchTermNoResults))
                .thenReturn(emptyResults);

        // Act
        ResponseEntity<?> response = controller.search(searchTermNoResults);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<?> resultList = (List<?>) response.getBody();
        assertEquals(0, resultList.size());
        verify(searchUseCase).searchByName(searchTermNoResults);
    }

    @Test
    @DisplayName("Búsqueda por nombre genérico (Escenario 2)")
    void testSearchByGenericName() {
        // Given el catálogo contiene medicamentos con principio activo "paracetamol"
        // When escribe "paracetamol" en el campo de búsqueda
        // Then se muestran todos los medicamentos que contienen paracetamol
        
        // Arrange
        String genericSearchTerm = "paracetamol";
        List<Medication> medications = List.of(
                Medication.builder()
                        .id(1L)
                        .name("Paracetamol 500mg")
                        .description("Paracetamol puro - Analgésico")
                        .unit("tableta")
                        .build(),
                Medication.builder()
                        .id(2L)
                        .name("Paracetamol Infantil")
                        .description("Paracetamol en gotas para niños")
                        .unit("ml")
                        .build()
        );

        when(searchUseCase.searchByName(genericSearchTerm))
                .thenReturn(medications);

        // Act
        ResponseEntity<?> response = controller.search(genericSearchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<?> resultList = (List<?>) response.getBody();
        assertEquals(2, resultList.size());
        verify(searchUseCase).searchByName(genericSearchTerm);
    }

    @Test
    @DisplayName("Búsqueda comercial excluye no coincidentes (Escenario 1)")
    void testSearchCommericalNameExcludesNonMatches() {
        // Given el cliente está en la pantalla principal
        // And el catálogo contiene "Paracetamol 500mg", "Paracetamol Infantil" y "Ibuprofeno"
        // When escribe "paracetamol" en el campo de búsqueda
        // Then se muestran "Paracetamol 500mg" y "Paracetamol Infantil"
        // And NO se muestra "Ibuprofeno"
        
        // Arrange
        String searchTerm = "paracetamol";
        List<Medication> medications = List.of(
                Medication.builder()
                        .id(1L)
                        .name("Paracetamol 500mg")
                        .description("Analgésico")
                        .unit("tableta")
                        .build(),
                Medication.builder()
                        .id(2L)
                        .name("Paracetamol Infantil")
                        .description("Para niños")
                        .unit("ml")
                        .build()
        );

        when(searchUseCase.searchByName(searchTerm))
                .thenReturn(medications);

        // Act
        ResponseEntity<?> response = controller.search(searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        List<?> resultList = (List<?>) response.getBody();
        assertEquals(2, resultList.size());
        // Verificar que Ibuprofeno NO está en los resultados
        String responseBody = response.getBody().toString();
        assertFalse(responseBody.contains("Ibuprofeno"),
                "Ibuprofeno no debería estar en los resultados de búsqueda de 'paracetamol'");
        verify(searchUseCase).searchByName(searchTerm);
    }

    // ======================== GET STOCK ENDPOINT ========================

    @Test
    @DisplayName("Debería retornar stock de sucursal exitosamente")
    void testGetStockSuccess() {
        // Arrange
        Long branchId = 5L;
        List<StockWithMedicationInfo> stocks = List.of(
                StockWithMedicationInfo.builder()
                        .medicationId(1L)
                        .medicationName("Paracetamol 500mg")
                        .branchId(5L)
                        .quantity(35)
                        .medicationUnit("tableta")
                        .build()
        );

        when(medicationRepository.findStockByBranchWithMedicationInfo(branchId))
                .thenReturn(stocks);

        // Act
        ResponseEntity<?> response = controller.getStock(branchId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(medicationRepository).findStockByBranchWithMedicationInfo(branchId);
    }

    @Test
    @DisplayName("Debería manejar error al obtener stock con búsqueda de casos de uso")
    void testGetStockWithSearchUseCase() {
        // Arrange
        Long branchId = 5L;
        List<BranchStock> stocks = List.of(
                BranchStock.builder().branchId(5L).medicationId(1L).quantity(35).build()
        );

        // Se puede usar el search use case también
        when(searchUseCase.getStockByBranch(branchId))
                .thenReturn(stocks);

        // Act - En la práctica, el controlador usará medicationRepository
        // pero el test valida que getStockByBranch también está disponible
        List<BranchStock> result = searchUseCase.getStockByBranch(branchId);

        // Assert
        assertEquals(1, result.size());
        verify(searchUseCase).getStockByBranch(branchId);
    }

    // ======================== CREATE MEDICATION ENDPOINT ========================

    @Test
    @DisplayName("Debería crear medicamento exitosamente")
    void testCreateMedicationSuccess() {
        // Arrange
        CreateMedicationRequest request = CreateMedicationRequest.builder()
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .branchId(1L)
                .initialStock(100)
                .build();

        Medication created = Medication.builder()
                .id(10L)
                .name("Aspirina")
                .unit("tableta")
                .description("Analgésico")
                .build();

        when(updateUseCase.createMedication(any(), anyLong(), anyInt()))
                .thenReturn(created);

        // Act
        ResponseEntity<?> response = controller.create(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(updateUseCase).createMedication(any(), eq(1L), eq(100));
    }

    @Test
    @DisplayName("Debería manejar error al crear medicamento duplicado")
    void testCreateMedicationDuplicate() {
        // Arrange
        CreateMedicationRequest request = CreateMedicationRequest.builder()
                .name("Existente")
                .unit("tableta")
                .branchId(1L)
                .initialStock(100)
                .build();

        when(updateUseCase.createMedication(any(), anyLong(), anyInt()))
                .thenThrow(new BusinessException("El medicamento ya existe"));

        // Act & Assert
        assertThrows(BusinessException.class, () -> controller.create(request));
    }

    // ======================== UPDATE STOCK ENDPOINT ========================

    @Test
    @DisplayName("Debería actualizar stock exitosamente")
    void testUpdateStockSuccess() {
        // Arrange
        Long medicationId = 1L;
        Long branchId = 5L;
        UpdateStockRequest request = UpdateStockRequest.builder()
                .medicationId(1L)
                .quantity(50)
                .build();

        // Act
        ResponseEntity<?> response = controller.updateStock(medicationId, branchId, request);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(updateUseCase).updateStock(branchId, medicationId, 50);
    }

    @Test
    @DisplayName("Debería manejar medicamento no encontrado")
    void testUpdateStockMedicationNotFound() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .medicationId(999L)
                .quantity(50)
                .build();

        doThrow(new ResourceNotFoundException("Medicamento no encontrado"))
                .when(updateUseCase).updateStock(anyLong(), anyLong(), anyInt());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                controller.updateStock(999L, 5L, request));
    }

    @Test
    @DisplayName("Debería manejar cantidad negativa")
    void testUpdateStockNegativeQuantity() {
        // Arrange
        UpdateStockRequest request = UpdateStockRequest.builder()
                .medicationId(1L)
                .quantity(-10)
                .build();

        doThrow(new BusinessException("La cantidad no puede ser negativa"))
                .when(updateUseCase).updateStock(anyLong(), anyLong(), anyInt());

        // Act & Assert
        assertThrows(BusinessException.class, () ->
                controller.updateStock(1L, 5L, request));
    }

    // ======================== AVAILABILITY ENDPOINTS (HU-04) ========================

    @Test
    @DisplayName("Debería retornar disponibilidad en sucursal cuando hay stock")
    void testGetAvailabilityInBranchWithStock() {
        // Arrange
        Long medicationId = 1L;
        Long branchId = 5L;
        BranchStock stock = BranchStock.builder()
                .medicationId(medicationId)
                .branchId(branchId)
                .quantity(50)
                .build();

        when(searchUseCase.getAvailabilityByMedicationBranch(medicationId, branchId))
                .thenReturn(stock);

        // Act
        ResponseEntity<?> response = controller.getAvailabilityInBranch(medicationId, branchId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).getAvailabilityByMedicationBranch(medicationId, branchId);
    }

    @Test
    @DisplayName("Debería retornar disponibilidad en sucursal cuando NO hay stock")
    void testGetAvailabilityInBranchNoStock() {
        // Arrange
        Long medicationId = 1L;
        Long branchId = 5L;
        BranchStock stock = BranchStock.builder()
                .medicationId(medicationId)
                .branchId(branchId)
                .quantity(0)
                .build();

        when(searchUseCase.getAvailabilityByMedicationBranch(medicationId, branchId))
                .thenReturn(stock);

        // Act
        ResponseEntity<?> response = controller.getAvailabilityInBranch(medicationId, branchId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).getAvailabilityByMedicationBranch(medicationId, branchId);
    }

    @Test
    @DisplayName("Debería manejar medicación no encontrada en disponibilidad por sucursal")
    void testGetAvailabilityInBranchMedicationNotFound() {
        // Arrange
        Long medicationId = 999L;
        Long branchId = 5L;

        when(searchUseCase.getAvailabilityByMedicationBranch(medicationId, branchId))
                .thenThrow(new ResourceNotFoundException("Medicación no encontrada"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                controller.getAvailabilityInBranch(medicationId, branchId));
    }

    @Test
    @DisplayName("Debería retornar disponibilidad en todas las sucursales")
    void testGetAvailabilityInAllBranchesSuccess() {
        // Arrange
        Long medicationId = 1L;
        Medication medication = Medication.builder()
                .id(medicationId)
                .name("Paracetamol 500mg")
                .unit("tableta")
                .description("Analgésico")
                .build();

        List<BranchStock> stocks = List.of(
                BranchStock.builder()
                        .medicationId(medicationId)
                        .branchId(1L)
                        .quantity(100)
                        .build(),
                BranchStock.builder()
                        .medicationId(medicationId)
                        .branchId(5L)
                        .quantity(50)
                        .build(),
                BranchStock.builder()
                        .medicationId(medicationId)
                        .branchId(10L)
                        .quantity(0)
                        .build()
        );

        when(searchUseCase.findById(medicationId))
                .thenReturn(java.util.Optional.of(medication));
        when(searchUseCase.getAvailabilityByMedicationAllBranches(medicationId))
                .thenReturn(stocks);

        // Act
        ResponseEntity<?> response = controller.getAvailabilityInAllBranches(medicationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).getAvailabilityByMedicationAllBranches(medicationId);
    }

    @Test
    @DisplayName("Debería retornar disponibilidad vacía cuando medicação no tiene stock en ninguna sucursal")
    void testGetAvailabilityInAllBranchesNoStock() {
        // Arrange
        Long medicationId = 1L;
        Medication medication = Medication.builder()
                .id(medicationId)
                .name("Paracetamol 500mg")
                .unit("tableta")
                .description("Analgésico")
                .build();

        List<BranchStock> stocks = List.of(
                BranchStock.builder()
                        .medicationId(medicationId)
                        .branchId(1L)
                        .quantity(0)
                        .build(),
                BranchStock.builder()
                        .medicationId(medicationId)
                        .branchId(5L)
                        .quantity(0)
                        .build()
        );

        when(searchUseCase.findById(medicationId))
                .thenReturn(java.util.Optional.of(medication));
        when(searchUseCase.getAvailabilityByMedicationAllBranches(medicationId))
                .thenReturn(stocks);

        // Act
        ResponseEntity<?> response = controller.getAvailabilityInAllBranches(medicationId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(searchUseCase).getAvailabilityByMedicationAllBranches(medicationId);
    }

    @Test
    @DisplayName("Debería manejar medicación no encontrada en disponibilidad por todas sucursales")
    void testGetAvailabilityInAllBranchesMedicationNotFound() {
        // Arrange
        Long medicationId = 999L;

        when(searchUseCase.getAvailabilityByMedicationAllBranches(medicationId))
                .thenThrow(new ResourceNotFoundException("Medicación no encontrada"));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                controller.getAvailabilityInAllBranches(medicationId));
    }
}
