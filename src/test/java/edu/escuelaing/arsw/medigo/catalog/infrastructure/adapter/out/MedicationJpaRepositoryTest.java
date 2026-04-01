package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.catalog.domain.model.BranchStock;
import edu.escuelaing.arsw.medigo.catalog.domain.model.Medication;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.MedicationEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchStockEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.MedicationSpringDataRepository;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.BranchStockSpringDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para el adaptador MedicationJpaRepository
 */
@DisplayName("MedicationJpaRepository - Adaptador de persistencia")
class MedicationJpaRepositoryTest {

    @Mock
    private MedicationSpringDataRepository medicationSpringDataRepository;

    @Mock
    private BranchStockSpringDataRepository branchStockSpringDataRepository;

    private MedicationJpaRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new MedicationJpaRepository(
                medicationSpringDataRepository,
                branchStockSpringDataRepository
        );
    }

    // ======================== FIND BY NAME ========================

    @Test
    @DisplayName("Debería buscar medicamentos por nombre exitosamente")
    void testFindByNameContainingSuccess() {
        // Arrange
        String searchTerm = "paracetamol";
        List<MedicationEntity> entities = List.of(
                MedicationEntity.builder()
                        .id(1L)
                        .name("Paracetamol 500mg")
                        .unit("tableta")
                        .build(),
                MedicationEntity.builder()
                        .id(2L)
                        .name("Paracetamol 1000mg")
                        .unit("tableta")
                        .build()
        );

        when(medicationSpringDataRepository.findByNameContainingIgnoreCase(searchTerm))
                .thenReturn(entities);

        // Act
        List<Medication> result = repository.findByNameContaining(searchTerm);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Paracetamol 500mg", result.get(0).getName());
        verify(medicationSpringDataRepository).findByNameContainingIgnoreCase(searchTerm);
    }

    @Test
    @DisplayName("Debería retornar lista vacía si no hay resultados")
    void testFindByNameContainingNoResults() {
        // Arrange
        when(medicationSpringDataRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(List.of());

        // Act
        List<Medication> result = repository.findByNameContaining("inexistente");

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== FIND BY ID ========================

    @Test
    @DisplayName("Debería encontrar medicamento por ID")
    void testFindByIdSuccess() {
        // Arrange
        MedicationEntity entity = MedicationEntity.builder()
                .id(1L)
                .name("Aspirina")
                .unit("tableta")
                .build();

        when(medicationSpringDataRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        // Act
        Optional<Medication> result = repository.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Aspirina", result.get().getName());
    }

    @Test
    @DisplayName("Debería retornar Optional vacío si no existe")
    void testFindByIdNotFound() {
        // Arrange
        when(medicationSpringDataRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // Act
        Optional<Medication> result = repository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== SAVE ========================

    @Test
    @DisplayName("Debería guardar medicamento exitosamente")
    void testSaveSuccess() {
        // Arrange
        Medication medication = Medication.builder()
                .name("Nuevo Medicamento")
                .unit("tableta")
                .description("Descripción")
                .build();

        MedicationEntity savedEntity = MedicationEntity.builder()
                .id(10L)
                .name("Nuevo Medicamento")
                .unit("tableta")
                .description("Descripción")
                .build();

        when(medicationSpringDataRepository.save(any(MedicationEntity.class)))
                .thenReturn(savedEntity);

        // Act
        Medication result = repository.save(medication);

        // Assert
        assertNotNull(result.getId());
        assertEquals(10L, result.getId());
        assertEquals("Nuevo Medicamento", result.getName());
        verify(medicationSpringDataRepository).save(any(MedicationEntity.class));
    }

    // ======================== FIND STOCK BY BRANCH ========================

    @Test
    @DisplayName("Debería obtener stocks de una sucursal")
    void testFindStockByBranchSuccess() {
        // Arrange
        Long branchId = 5L;
        List<BranchStockEntity> entities = List.of(
                BranchStockEntity.builder()
                        .id(1L)
                        .branchId(5L)
                        .medicationId(1L)
                        .quantity(35)
                        .build(),
                BranchStockEntity.builder()
                        .id(2L)
                        .branchId(5L)
                        .medicationId(2L)
                        .quantity(0)
                        .build()
        );

        when(branchStockSpringDataRepository.findByBranchId(branchId))
                .thenReturn(entities);

        // Act
        List<BranchStock> result = repository.findStockByBranch(branchId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(5L, result.get(0).getBranchId());
        verify(branchStockSpringDataRepository).findByBranchId(branchId);
    }

    @Test
    @DisplayName("Debería retornar lista vacía si sucursal no tiene stock")
    void testFindStockByBranchEmpty() {
        // Arrange
        when(branchStockSpringDataRepository.findByBranchId(anyLong()))
                .thenReturn(List.of());

        // Act
        List<BranchStock> result = repository.findStockByBranch(5L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ======================== UPDATE STOCK ========================

    @Test
    @DisplayName("Debería actualizar stock existente")
    void testUpdateStockExisting() {
        // Arrange
        Long branchId = 5L;
        Long medicationId = 1L;
        int quantity = 50;

        BranchStockEntity existing = BranchStockEntity.builder()
                .id(1L)
                .branchId(5L)
                .medicationId(1L)
                .quantity(35)
                .build();

        when(branchStockSpringDataRepository.findByBranchIdAndMedicationId(branchId, medicationId))
                .thenReturn(Optional.of(existing));

        when(branchStockSpringDataRepository.save(any(BranchStockEntity.class)))
                .thenReturn(existing);

        // Act
        repository.updateStock(branchId, medicationId, quantity);

        // Assert
        verify(branchStockSpringDataRepository).findByBranchIdAndMedicationId(branchId, medicationId);
        verify(branchStockSpringDataRepository).save(any(BranchStockEntity.class));
    }

    @Test
    @DisplayName("Debería crear stock nuevo si no existe")
    void testUpdateStockCreating() {
        // Arrange
        Long branchId = 5L;
        Long medicationId = 1L;
        int quantity = 50;

        when(branchStockSpringDataRepository.findByBranchIdAndMedicationId(branchId, medicationId))
                .thenReturn(Optional.empty());

        BranchStockEntity newEntity = BranchStockEntity.builder()
                .branchId(5L)
                .medicationId(1L)
                .quantity(50)
                .build();

        when(branchStockSpringDataRepository.save(any(BranchStockEntity.class)))
                .thenReturn(newEntity);

        // Act
        repository.updateStock(branchId, medicationId, quantity);

        // Assert
        verify(branchStockSpringDataRepository).findByBranchIdAndMedicationId(branchId, medicationId);
        verify(branchStockSpringDataRepository).save(any(BranchStockEntity.class));
    }

    @Test
    @DisplayName("Debería permitir actualizar stock a cero")
    void testUpdateStockToZero() {
        // Arrange
        BranchStockEntity existing = BranchStockEntity.builder()
                .id(1L)
                .branchId(5L)
                .medicationId(1L)
                .quantity(35)
                .build();

        when(branchStockSpringDataRepository.findByBranchIdAndMedicationId(anyLong(), anyLong()))
                .thenReturn(Optional.of(existing));

        when(branchStockSpringDataRepository.save(any(BranchStockEntity.class)))
                .thenReturn(existing);

        // Act
        repository.updateStock(5L, 1L, 0);

        // Assert
        verify(branchStockSpringDataRepository).save(any(BranchStockEntity.class));
    }
}
