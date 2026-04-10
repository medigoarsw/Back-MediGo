package edu.escuelaing.arsw.medigo.auction.infrastructure.config;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Utilidad central para tiempo de subastas en zona horaria de Colombia.
 */
public final class AuctionTime {

    private static final ZoneId COLOMBIA_ZONE = ZoneId.of("America/Bogota");

    private AuctionTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(COLOMBIA_ZONE);
    }

    public static LocalDateTime adjustForStorage(LocalDateTime dateTime) {
        return dateTime;
    }
}
