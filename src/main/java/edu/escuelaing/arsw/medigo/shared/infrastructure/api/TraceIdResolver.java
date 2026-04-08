package edu.escuelaing.arsw.medigo.shared.infrastructure.api;

import java.util.UUID;

public final class TraceIdResolver {

    private TraceIdResolver() {
    }

    public static String resolve(String headerTraceId) {
        if (headerTraceId == null || headerTraceId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return headerTraceId.trim();
    }
}
