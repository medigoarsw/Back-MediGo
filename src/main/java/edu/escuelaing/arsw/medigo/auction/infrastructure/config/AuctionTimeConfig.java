package edu.escuelaing.arsw.medigo.auction.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuctionTimeConfig {

    public AuctionTimeConfig(@Value("${app.auction.time-offset-hours:0}") int offsetHours) {
        AuctionTime.setOffsetHours(offsetHours);
    }
}
