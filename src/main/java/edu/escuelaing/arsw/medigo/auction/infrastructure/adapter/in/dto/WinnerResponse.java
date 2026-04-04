package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in.dto;

import java.math.BigDecimal;

public record WinnerResponse(
    Long       auctionId,
    Long       winnerId,
    String     winnerName,
    BigDecimal winningAmount
) {}
