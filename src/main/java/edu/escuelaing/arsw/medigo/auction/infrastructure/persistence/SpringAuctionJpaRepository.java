package edu.escuelaing.arsw.medigo.auction.infrastructure.persistence;

import edu.escuelaing.arsw.medigo.auction.infrastructure.entity.AuctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SpringAuctionJpaRepository extends JpaRepository<AuctionEntity, Long> {

    List<AuctionEntity> findByStatus(String status);

    // Subastas activas cuyo endTime ya paso (cierre por tiempo fijo)
    @Query("SELECT a FROM AuctionEntity a WHERE a.status = 'ACTIVE' AND a.endTime <= :now")
    List<AuctionEntity> findExpiredActive(@Param("now") LocalDateTime now);

    // Subastas SCHEDULED cuyo startTime ya llego
    @Query("SELECT a FROM AuctionEntity a WHERE a.status = 'SCHEDULED' AND a.startTime <= :now")
    List<AuctionEntity> findScheduledReadyToStart(@Param("now") LocalDateTime now);

    // Verifica si ya existe subasta activa/programada para un medicamento
    @Query("SELECT COUNT(a) > 0 FROM AuctionEntity a " +
           "WHERE a.medicationId = :medicationId " +
           "AND a.status IN ('ACTIVE', 'SCHEDULED')")
    boolean existsActiveOrScheduledForMedication(@Param("medicationId") Long medicationId);
}
