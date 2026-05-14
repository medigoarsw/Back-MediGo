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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SedeAdminService - Unit Tests")
class SedeAdminServiceTest {

    @Mock
    private BranchSpringDataRepository branchRepository;

    @InjectMocks
    private SedeAdminService sedeAdminService;

    private BranchEntity mockBranch;
    private SedeRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockBranch = BranchEntity.builder()
                .id(1L)
                .name("Sede Central")
                .address("Calle 123")
                .specialty("General")
                .phone("1234567")
                .capacity(100)
                .latitude(4.0)
                .longitude(-74.0)
                .active(true)
                .build();

        mockRequest = new SedeRequest();
        mockRequest.setNombre("Sede Central");
        mockRequest.setDireccion("Calle 123");
        mockRequest.setEspecialidad("General");
        mockRequest.setTelefono("1234567");
        mockRequest.setCapacidad(100);
        mockRequest.setLatitude(4.0);
        mockRequest.setLongitude(-74.0);
    }

    @Test
    @DisplayName("list - Success")
    void list_success() {
        Page<BranchEntity> page = new PageImpl<>(List.of(mockBranch));
        when(branchRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<BranchEntity> result = sedeAdminService.list(1, 10, "Central");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Sede Central", result.getContent().get(0).getName());
    }

    @Test
    @DisplayName("list - Invalid page throws Exception")
    void list_invalidPage_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> sedeAdminService.list(0, 10, ""));
    }

    @Test
    @DisplayName("list - Invalid limit throws Exception")
    void list_invalidLimit_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> sedeAdminService.list(1, 150, ""));
    }

    @Test
    @DisplayName("getById - Success")
    void getById_success() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));

        BranchEntity result = sedeAdminService.getById(1L);

        assertNotNull(result);
        assertEquals("Sede Central", result.getName());
    }

    @Test
    @DisplayName("getById - Not found throws ResourceNotFoundException")
    void getById_notFound_throwsException() {
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sedeAdminService.getById(1L));
    }

    @Test
    @DisplayName("getById - Inactive throws ResourceNotFoundException")
    void getById_inactive_throwsException() {
        mockBranch.setActive(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));

        assertThrows(ResourceNotFoundException.class, () -> sedeAdminService.getById(1L));
    }

    @Test
    @DisplayName("create - Success")
    void create_success() {
        when(branchRepository.existsByNameIgnoreCaseAndActiveTrue("Sede Central")).thenReturn(false);
        when(branchRepository.save(any(BranchEntity.class))).thenReturn(mockBranch);

        BranchEntity result = sedeAdminService.create(mockRequest);

        assertNotNull(result);
        assertEquals("Sede Central", result.getName());
        verify(branchRepository).save(any(BranchEntity.class));
    }

    @Test
    @DisplayName("create - Conflict throws ResourceConflictException")
    void create_conflict_throwsException() {
        when(branchRepository.existsByNameIgnoreCaseAndActiveTrue("Sede Central")).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> sedeAdminService.create(mockRequest));
        verify(branchRepository, never()).save(any(BranchEntity.class));
    }

    @Test
    @DisplayName("create - Empty name throws Exception")
    void create_emptyName_throwsException() {
        mockRequest.setNombre("");

        assertThrows(IllegalArgumentException.class, () -> sedeAdminService.create(mockRequest));
    }

    @Test
    @DisplayName("update - Success")
    void update_success() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(branchRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot("Sede Nueva", 1L)).thenReturn(false);
        
        SedeUpdateRequest updateRequest = new SedeUpdateRequest();
        updateRequest.setNombre("Sede Nueva");
        updateRequest.setDireccion("Calle 456");
        updateRequest.setEspecialidad("Pediatria");
        updateRequest.setTelefono("987654");
        updateRequest.setCapacidad(200);
        updateRequest.setLatitude(5.0);
        updateRequest.setLongitude(-75.0);
        
        BranchEntity updatedBranch = BranchEntity.builder()
                .id(1L)
                .name("Sede Nueva")
                .address("Calle 456")
                .specialty("Pediatria")
                .phone("987654")
                .capacity(200)
                .latitude(5.0)
                .longitude(-75.0)
                .active(true)
                .build();
        
        when(branchRepository.save(any(BranchEntity.class))).thenReturn(updatedBranch);

        BranchEntity result = sedeAdminService.update(1L, updateRequest);

        assertNotNull(result);
        assertEquals("Sede Nueva", result.getName());
    }

    @Test
    @DisplayName("update - Conflict throws ResourceConflictException")
    void update_conflict_throwsException() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(branchRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot("Sede Nueva", 1L)).thenReturn(true);
        
        SedeUpdateRequest updateRequest = new SedeUpdateRequest();
        updateRequest.setNombre("Sede Nueva");

        assertThrows(ResourceConflictException.class, () -> sedeAdminService.update(1L, updateRequest));
    }

    @Test
    @DisplayName("softDelete - Success")
    void softDelete_success() {
        when(branchRepository.findById(1L)).thenReturn(Optional.of(mockBranch));
        when(branchRepository.save(any(BranchEntity.class))).thenReturn(mockBranch);

        sedeAdminService.softDelete(1L);

        assertFalse(mockBranch.getActive());
        verify(branchRepository).save(mockBranch);
    }
}
