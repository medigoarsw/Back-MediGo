package edu.escuelaing.arsw.medigo.auction.domain.port.out;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionOrderPort {

    Long createAuctionOrder(Long auctionId, Long winnerId, Long medicationId,
                            BigDecimal finalPrice, Long branchId);

    Optional<String> getOrderStatus(Long orderId);

    void cancelOrder(Long orderId);

    Optional<Long> findOrderIdByAuction(Long auctionId);

    List<ExpiredAuctionOrder> findExpiredPendingOrders(LocalDateTime cutoff);

    record ExpiredAuctionOrder(Long orderId, Long auctionId, Long winnerId) {}
}
