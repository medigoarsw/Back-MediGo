package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.auction.domain.model.Auction;
import edu.escuelaing.arsw.medigo.auction.domain.model.Bid;
import edu.escuelaing.arsw.medigo.auction.domain.port.in.*;
import edu.escuelaing.arsw.medigo.shared.infrastructure.config.SecurityConfig;
import edu.escuelaing.arsw.medigo.shared.infrastructure.security.AuthenticatedUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de autorización por rol en el módulo de subastas.
 *
 * Escenarios cubiertos:
 *   DELIVERY   → 403 en todos los endpoints de subastas.
 *   AFFILIATE  → 403 en crear/editar; 2xx en consultar/unirse/pujar.
 *   ADMIN      → 2xx en todos los endpoints.
 *
 * El token MVP tiene formato: "fake-jwt.{userId}.{ROLE}.{timestamp}"
 * Este mismo formato es parseado por JwtAuthenticationFilter.
 */
@WebMvcTest(value = AuctionController.class,
            excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(SecurityConfig.class)
@DisplayName("AuctionController - Autorización por rol")
class AuctionControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockBean private CreateAuctionUseCase createAuctionUseCase;
    @MockBean private UpdateAuctionUseCase updateAuctionUseCase;
    @MockBean private PlaceBidUseCase      placeBidUseCase;
    @MockBean private QueryAuctionUseCase  queryAuctionUseCase;
    @MockBean private JoinAuctionUseCase   joinAuctionUseCase;
    @MockBean private AuthenticatedUserResolver authenticatedUserResolver;

    // ── Tokens de prueba ──────────────────────────────────────────────
    private static final String ADMIN_TOKEN     = "Bearer fake-jwt.1.ADMIN.1700000000000";
    private static final String AFFILIATE_TOKEN = "Bearer fake-jwt.2.AFFILIATE.1700000000000";
    private static final String DELIVERY_TOKEN  = "Bearer fake-jwt.3.DELIVERY.1700000000000";

    @BeforeEach
    void configurarStubs() {
        Auction subasta = buildAuction();
        QueryAuctionUseCase.AuctionDetailView detalle =
            new QueryAuctionUseCase.AuctionDetailView(
                subasta, "Ibuprofeno", "mg", Duration.ofHours(2), null, BigDecimal.valueOf(6000));

        when(queryAuctionUseCase.getAuctionDetail(anyLong())).thenReturn(detalle);
        when(queryAuctionUseCase.getActiveAuctions()).thenReturn(List.of());
        when(queryAuctionUseCase.getBidHistory(anyLong())).thenReturn(List.of());
        when(authenticatedUserResolver.getAuthenticatedUserId()).thenReturn(2L);
        when(queryAuctionUseCase.getWonAuctionsByAffiliate(anyLong(), anyInt(), anyInt()))
            .thenReturn(new QueryAuctionUseCase.WonAuctionsPageView(List.of(), 0, 20, 0, 0));
        when(createAuctionUseCase.createAuction(any())).thenReturn(subasta);
        when(updateAuctionUseCase.updateAuction(anyLong(), any())).thenReturn(subasta);
        when(placeBidUseCase.placeBid(anyLong(), anyLong(), anyString(), any()))
            .thenReturn(buildBid());
        // joinAuctionUseCase.joinAuction es void → Mockito hace nothing() por defecto
    }

    // ═════════════════════════════════════════════════════════════════
    //  DELIVERY – bloqueado en todos los endpoints de subastas (403)
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("DELIVERY | GET /api/auctions/{id} → 403")
    void delivery_getById_retorna403() throws Exception {
        mvc.perform(get("/api/auctions/1")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | GET /api/auctions/active → 403")
    void delivery_getActive_retorna403() throws Exception {
        mvc.perform(get("/api/auctions/active")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | GET /api/auctions/won → 403")
    void delivery_getWon_retorna403() throws Exception {
        mvc.perform(get("/api/auctions/won")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | GET /api/auctions/{id}/bids → 403")
    void delivery_getBids_retorna403() throws Exception {
        mvc.perform(get("/api/auctions/1/bids")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | POST /api/auctions/{id}/join → 403")
    void delivery_join_retorna403() throws Exception {
        mvc.perform(post("/api/auctions/1/join")
                .param("userId", "3")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | POST /api/auctions/{id}/bids → 403")
    void delivery_placeBid_retorna403() throws Exception {
        mvc.perform(post("/api/auctions/1/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":3,\"userName\":\"Delivery\",\"amount\":6000}")
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | POST /api/auctions → 403")
    void delivery_create_retorna403() throws Exception {
        mvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearAuctionJson())
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELIVERY | PUT /api/auctions/{id} → 403")
    void delivery_update_retorna403() throws Exception {
        mvc.perform(put("/api/auctions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAuctionJson())
                .header("Authorization", DELIVERY_TOKEN))
           .andExpect(status().isForbidden());
    }

    // ═════════════════════════════════════════════════════════════════
    //  AFFILIATE (USUARIO) – 403 solo en crear y editar
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AFFILIATE | POST /api/auctions → 403")
    void affiliate_create_retorna403() throws Exception {
        mvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearAuctionJson())
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AFFILIATE | PUT /api/auctions/{id} → 403")
    void affiliate_update_retorna403() throws Exception {
        mvc.perform(put("/api/auctions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAuctionJson())
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isForbidden());
    }

    // ═════════════════════════════════════════════════════════════════
    //  AFFILIATE (USUARIO) – 2xx en los endpoints permitidos
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("AFFILIATE | GET /api/auctions/{id} → 200")
    void affiliate_getById_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/1")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AFFILIATE | GET /api/auctions/active → 200")
    void affiliate_getActive_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/active")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AFFILIATE | GET /api/auctions/won → 200")
    void affiliate_getWon_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/won")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AFFILIATE | GET /api/auctions/{id}/bids → 200")
    void affiliate_getBids_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/1/bids")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AFFILIATE | POST /api/auctions/{id}/join → 204")
    void affiliate_join_retorna204() throws Exception {
        mvc.perform(post("/api/auctions/1/join")
                .param("userId", "2")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("AFFILIATE | POST /api/auctions/{id}/bids → 201")
    void affiliate_placeBid_retorna201() throws Exception {
        mvc.perform(post("/api/auctions/1/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":2,\"userName\":\"Affiliate\",\"amount\":6000}")
                .header("Authorization", AFFILIATE_TOKEN))
           .andExpect(status().isCreated());
    }

    // ═════════════════════════════════════════════════════════════════
    //  ADMIN – acceso total (2xx en todos los endpoints)
    // ═════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("ADMIN | POST /api/auctions → 201")
    void admin_create_retorna201() throws Exception {
        mvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearAuctionJson())
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("ADMIN | PUT /api/auctions/{id} → 200")
    void admin_update_retorna200() throws Exception {
        mvc.perform(put("/api/auctions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAuctionJson())
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN | GET /api/auctions/{id} → 200")
    void admin_getById_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/1")
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN | GET /api/auctions/won → 200")
    void admin_getWon_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/won")
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN | GET /api/auctions/{id}/bids → 200")
    void admin_getBids_retorna200() throws Exception {
        mvc.perform(get("/api/auctions/1/bids")
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN | POST /api/auctions/{id}/join → 204")
    void admin_join_retorna204() throws Exception {
        mvc.perform(post("/api/auctions/1/join")
                .param("userId", "1")
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("ADMIN | POST /api/auctions/{id}/bids → 201")
    void admin_placeBid_retorna201() throws Exception {
        mvc.perform(post("/api/auctions/1/bids")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"userName\":\"Admin\",\"amount\":6000}")
                .header("Authorization", ADMIN_TOKEN))
           .andExpect(status().isCreated());
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private Auction buildAuction() {
        return Auction.builder()
            .id(1L).medicationId(1L).branchId(1L)
            .basePrice(BigDecimal.valueOf(5000))
            .startTime(LocalDateTime.now().minusMinutes(10))
            .endTime(LocalDateTime.now().plusHours(2))
            .status(Auction.AuctionStatus.ACTIVE)
            .closureType(Auction.ClosureType.FIXED_TIME)
            .build();
    }

    private Bid buildBid() {
        return Bid.builder()
            .id(1L).auctionId(1L).userId(2L)
            .userName("Test").amount(BigDecimal.valueOf(6000))
            .placedAt(LocalDateTime.now())
            .build();
    }

    private String crearAuctionJson() {
        return """
            {
              "medicationId": 1,
              "branchId": 1,
              "basePrice": 5000,
              "startTime": [2027,6,1,10,0,0],
              "endTime":   [2027,6,1,12,0,0]
            }
            """;
    }

    private String updateAuctionJson() {
        return """
            {
              "basePrice": 6000,
              "startTime": [2027,6,1,10,0,0],
              "endTime":   [2027,6,1,12,0,0]
            }
            """;
    }
}
