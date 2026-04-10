package edu.escuelaing.arsw.medigo.logistics.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Servicio de Broadcast para Producción.
 * Solo reenvía ubicaciones de repartidores reales conectados.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final edu.escuelaing.arsw.medigo.logistics.domain.port.out.DeliveryRepositoryPort deliveryRepository;
    private final Map<String, Map<String, Object>> locations = new ConcurrentHashMap<>();

    public void updateLocation(String id, Object location) {
        if (location instanceof Map) {
            Map<String, Object> locMap = new java.util.HashMap<>((Map<String, Object>) location);
            locMap.put("id", id);
            
            // Vincular con entrega activa real si existe, incluyendo su estado
            try {
                Long driverId = Long.parseLong(id);
                deliveryRepository.findActiveByDeliveryPersonId(driverId).stream().findFirst().ifPresent(delivery -> {
                    locMap.put("orderId", delivery.getOrderId());
                    // "ASSIGNED" = va a recoger, "IN_ROUTE" = va a entregar
                    locMap.put("deliveryStatus", delivery.getStatus().name());
                });
            } catch (Exception ignored) {}
            // Si no tiene entrega activa, deliveryStatus no se incluye (libre)

            locations.put(id, locMap);
            // Notificar a todos los interesados (clientes y otros repartidores)
            messagingTemplate.convertAndSend("/topic/deliveries", locMap);
        }
    }
}
