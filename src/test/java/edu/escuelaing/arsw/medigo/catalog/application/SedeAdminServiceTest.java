package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeUpdateRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.BranchSpringDataRepository;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceConflictException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SedeAdminService")
class SedeAdminServiceTest {

    @Mock
    private BranchSpringDataRepository branchRepository;

    private SedeAdminService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new SedeAdminService(branchRepository);
    }

    @Test
    @DisplayName("create debe rechazar nombre duplicado")
    void createRejectsDuplicateName() {
        SedeRequest request = new SedeRequest();
        request.setNombre("Sede Centro");
        request.setDireccion("Calle 10");
        request.setEspecialidad("General");

        when(branchRepository.existsByNameIgnoreCaseAndActiveTrue("Sede Centro")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> service.create(request));
    }

    @Test
    @DisplayName("update parcial debe aplicar solo campos presentes")
    void updatePartialWorks() {
        BranchEntity existing = BranchEntity.builder()
                .id(5L)
                .name("Sede Norte")
                .address("Dir vieja")
                .specialty("General")
                .phone("+57 300 000 0000")
                .capacity(100)
                .active(true)
                .build();

        SedeUpdateRequest request = new SedeUpdateRequest();
        request.setDireccion("Dir nueva");
        request.setCapacidad(180);

        when(branchRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(branchRepository.save(any(BranchEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BranchEntity updated = service.update(5L, request);

        assertEquals("Sede Norte", updated.getName());
        assertEquals("Dir nueva", updated.getAddress());
        assertEquals(180, updated.getCapacity());
    }

    @Test
    @DisplayName("softDelete debe marcar active=false")
    void softDeleteMarksInactive() {
        BranchEntity existing = BranchEntity.builder().id(9L).name("Sede Sur").active(true).build();
        when(branchRepository.findById(9L)).thenReturn(Optional.of(existing));

        service.softDelete(9L);

        assertEquals(false, existing.getActive());
        verify(branchRepository).save(existing);
    }

    @Test
    @DisplayName("getById debe lanzar not found si sede esta inactiva")
    void getByIdInactiveThrowsNotFound() {
        BranchEntity existing = BranchEntity.builder().id(4L).name("Sede X").active(false).build();
        when(branchRepository.findById(4L)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class, () -> service.getById(4L));
    }
}
