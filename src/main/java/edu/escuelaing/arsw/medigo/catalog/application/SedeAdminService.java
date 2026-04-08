package edu.escuelaing.arsw.medigo.catalog.application;

import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in.dto.SedeUpdateRequest;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.repository.BranchSpringDataRepository;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceConflictException;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SedeAdminService {

    private final BranchSpringDataRepository branchRepository;

    @Transactional(readOnly = true)
    public Page<BranchEntity> list(int page, int limit, String q) {
        if (page < 1) {
            throw new IllegalArgumentException("page debe ser >= 1");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit debe estar entre 1 y 100");
        }

        Specification<BranchEntity> spec = Specification.where(isActive());
        String normalizedQ = trimToNull(q);
        if (normalizedQ != null) {
            spec = spec.and(containsText(normalizedQ));
        }

        return branchRepository.findAll(
                spec,
                PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "id"))
        );
    }

    @Transactional(readOnly = true)
    public BranchEntity getById(Long id) {
        return branchRepository.findById(id)
                .filter(branch -> Boolean.TRUE.equals(branch.getActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada", String.valueOf(id)));
    }

    @Transactional
    public BranchEntity create(SedeRequest request) {
        String nombre = requiredTrim(request.getNombre(), "nombre");
        String direccion = requiredTrim(request.getDireccion(), "direccion");
        String especialidad = requiredTrim(request.getEspecialidad(), "especialidad");
        String telefono = optionalTrim(request.getTelefono());

        if (branchRepository.existsByNameIgnoreCaseAndActiveTrue(nombre)) {
            throw new ResourceConflictException("Ya existe una sede activa con ese nombre");
        }

        BranchEntity entity = BranchEntity.builder()
                .name(nombre)
                .address(direccion)
                .specialty(especialidad)
                .phone(telefono)
                .capacity(request.getCapacidad())
                .active(true)
                .build();

        BranchEntity saved = branchRepository.save(entity);
        log.info("Sede creada con id={}", saved.getId());
        return saved;
    }

    @Transactional
    public BranchEntity update(Long id, SedeUpdateRequest request) {
        BranchEntity entity = getById(id);

        if (request.getNombre() != null) {
            String nombre = requiredTrim(request.getNombre(), "nombre");
            if (branchRepository.existsByNameIgnoreCaseAndActiveTrueAndIdNot(nombre, id)) {
                throw new ResourceConflictException("Ya existe una sede activa con ese nombre");
            }
            entity.setName(nombre);
        }

        if (request.getDireccion() != null) {
            entity.setAddress(requiredTrim(request.getDireccion(), "direccion"));
        }

        if (request.getEspecialidad() != null) {
            entity.setSpecialty(requiredTrim(request.getEspecialidad(), "especialidad"));
        }

        if (request.getTelefono() != null) {
            entity.setPhone(optionalTrim(request.getTelefono()));
        }

        if (request.getCapacidad() != null) {
            entity.setCapacity(request.getCapacidad());
        }

        BranchEntity saved = branchRepository.save(entity);
        log.info("Sede actualizada id={}", id);
        return saved;
    }

    @Transactional
    public void softDelete(Long id) {
        BranchEntity entity = getById(id);
        entity.setActive(false);
        branchRepository.save(entity);
        log.info("Sede desactivada id={}", id);
    }

    private Specification<BranchEntity> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    private Specification<BranchEntity> containsText(String q) {
        String like = "%" + q.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(cb.coalesce(root.get("name"), "")), like),
                cb.like(cb.lower(cb.coalesce(root.get("address"), "")), like),
                cb.like(cb.lower(cb.coalesce(root.get("specialty"), "")), like)
        );
    }

    private String requiredTrim(String value, String fieldName) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " no puede estar vacio");
        }
        return normalized;
    }

    private String optionalTrim(String value) {
        return trimToNull(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
