package edu.escuelaing.arsw.medigo.orders.infrastructure.adapter.out.util;

import java.time.LocalDate;
import java.util.Random;

/**
 * Utilidad para generar números de orden únicos.
 * Formato: ORD-YYYY-XXXXXX
 * Ejemplo: ORD-2024-001234
 */
public class OrderNumberGenerator {
    
    private static final String PREFIX = "ORD";
    private static final String SEPARATOR = "-";
    private static final Random RANDOM = new Random();
    
    /**
     * Genera un número de orden único.
     * @return número de orden en formato: ORD-YYYY-XXXXXX
     */
    public static String generateOrderNumber() {
        int year = LocalDate.now().getYear();
        int randomNumber = 100000 + RANDOM.nextInt(900000); // Genera número entre 100000 y 999999
        return String.format("%s%s%d%s%06d", PREFIX, SEPARATOR, year, SEPARATOR, randomNumber);
    }
}
