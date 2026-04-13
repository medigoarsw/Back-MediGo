package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler para verificar que el backend está completamente listo para recibir peticiones.
 * Usado por el Gateway para warmup después del startup.
 */
@Slf4j
@RestController
@Hidden // No mostrar en Swagger
public class ReadinessProbeController {

    @GetMapping("/internal/ready")
    public ResponseEntity<String> readiness() {
        log.debug("Readiness probe check");
        return ResponseEntity.ok("READY");
    }
}
