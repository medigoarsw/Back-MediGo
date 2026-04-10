package edu.escuelaing.arsw.medigo.auction.infrastructure.config;

import java.time.LocalDateTime;

/**
 * Utilidad central para tiempo de subastas con offset configurable.
 */
public final class AuctionTime {

    private static volatile int offsetHours = 0;

    private AuctionTime() {
    }

    public static void setOffsetHours(int hours) {
        offsetHours = hours;
    }

    public static LocalDateTime now() {
        return LocalDateTime.now().plusHours(offsetHours);
    }

    public static LocalDateTime adjustForStorage(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(offsetHours);
    }
}
