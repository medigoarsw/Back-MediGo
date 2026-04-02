package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.port.out.AuctionCatalogPort;
import edu.escuelaing.arsw.medigo.catalog.domain.port.out.MedicationRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogAuctionAdapter implements AuctionCatalogPort {

    private final MedicationRepositoryPort medicationRepository;

    @Override
    public Optional<MedicationInfo> getMedicationInfo(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .map(m -> new MedicationInfo(m.getId(), m.getName(), m.getUnit()));
    }

    @Override
    public void reserveStock(Long branchId, Long medicationId) {
        medicationRepository.findStockByBranch(branchId).stream()
                .filter(s -> medicationId.equals(s.getMedicationId()))
                .findFirst()
                .ifPresentOrElse(
                    s -> {
                        int newQty = Math.max(0, s.getQuantity() - 1);
                        medicationRepository.updateStock(branchId, medicationId, newQty);
                        log.info("Stock reservado - sucursal={}, medicamento={}, nuevo stock={}",
                                branchId, medicationId, newQty);
                    },
                    () -> log.warn("No se encontró stock para reservar - sucursal={}, medicamento={}",
                            branchId, medicationId)
                );
    }

    @Override
    public void releaseStock(Long branchId, Long medicationId) {
        medicationRepository.findStockByBranch(branchId).stream()
                .filter(s -> medicationId.equals(s.getMedicationId()))
                .findFirst()
                .ifPresentOrElse(
                    s -> {
                        medicationRepository.updateStock(branchId, medicationId, s.getQuantity() + 1);
                        log.info("Stock liberado - sucursal={}, medicamento={}", branchId, medicationId);
                    },
                    () -> log.warn("No se encontró stock para liberar - sucursal={}, medicamento={}",
                            branchId, medicationId)
                );
    }
}
