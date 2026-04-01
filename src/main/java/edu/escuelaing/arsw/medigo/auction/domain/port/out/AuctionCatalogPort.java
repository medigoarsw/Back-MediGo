package edu.escuelaing.arsw.medigo.auction.domain.port.out;

import java.util.Optional;

public interface AuctionCatalogPort {

    Optional<MedicationInfo> getMedicationInfo(Long medicationId);

    void reserveStock(Long branchId, Long medicationId);

    void releaseStock(Long branchId, Long medicationId);

    record MedicationInfo(Long id, String name, String unit) {}
}
