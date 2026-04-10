package edu.escuelaing.arsw.medigo.shared.infrastructure.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.arsw.medigo.shared.infrastructure.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
public class AuthenticatedUserResolver {

    private static final String FAKE_JWT_PREFIX = "fake-jwt";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> USER_ID_CLAIMS = List.of("userId", "user_id", "uid", "sub", "id");

    public Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException("No se encontró usuario autenticado", "AUTHENTICATION_ERROR");
        }

        Object principal = authentication.getPrincipal();
        String principalStr = String.valueOf(principal);

        // Si ya es un ID numérico (resuelto previamente por el filtro de seguridad)
        Long userId = parseLongOrNull(principalStr);
        if (userId != null) {
            return userId;
        }

        // Si no es numérico, intentamos tratarlo como token (Fake o Real)
        userId = principalStr.startsWith(FAKE_JWT_PREFIX)
                ? extractUserIdFromFakeToken(principalStr)
                : extractUserIdFromRealJwt(principalStr);

        if (userId == null) {
            throw new BusinessException("No se pudo resolver el usuario autenticado", "AUTHENTICATION_ERROR");
        }

        return userId;
    }

    private Long extractUserIdFromFakeToken(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length < 4) {
            return null;
        }
        return parseLongOrNull(parts[1]);
    }

    private Long extractUserIdFromRealJwt(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            return null;
        }

        try {
            byte[] decodedPayload = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            JsonNode payload = MAPPER.readTree(new String(decodedPayload, StandardCharsets.UTF_8));

            for (String claim : USER_ID_CLAIMS) {
                JsonNode node = payload.get(claim);
                if (node == null || node.isNull()) {
                    continue;
                }
                if (node.isNumber()) {
                    return node.asLong();
                }
                if (node.isTextual()) {
                    Long parsed = parseLongOrNull(node.asText());
                    if (parsed != null) {
                        return parsed;
                    }
                }
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private Long parseLongOrNull(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String padBase64(String input) {
        int remainder = input.length() % 4;
        return remainder == 0 ? input : input + "=".repeat(4 - remainder);
    }
}
