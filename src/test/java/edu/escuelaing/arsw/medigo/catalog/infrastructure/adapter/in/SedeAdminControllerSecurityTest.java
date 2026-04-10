package edu.escuelaing.arsw.medigo.catalog.infrastructure.adapter.in;

import edu.escuelaing.arsw.medigo.catalog.application.SedeAdminService;
import edu.escuelaing.arsw.medigo.catalog.infrastructure.entity.BranchEntity;
import edu.escuelaing.arsw.medigo.shared.infrastructure.config.SecurityConfig;
import edu.escuelaing.arsw.medigo.shared.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SedeAdminController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({SecurityConfig.class, SedeExceptionHandler.class})
@DisplayName("SedeAdminController - Seguridad y contrato")
class SedeAdminControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SedeAdminService sedeService;

    @MockBean
    private JwtService jwtService;

    private static final String ADMIN_TOKEN = "Bearer fake-jwt.1.ADMIN.1700000000000";
    private static final String AFFILIATE_TOKEN = "Bearer fake-jwt.2.AFFILIATE.1700000000000";

    @Test
        @DisplayName("GET /api/sedes con AFFILIATE retorna 403")
    void listWithAffiliateReturnsForbidden() throws Exception {
                mvc.perform(get("/api/sedes")
                        .header("Authorization", AFFILIATE_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
        @DisplayName("GET /api/sedes con ADMIN retorna envelope exitoso")
    void listWithAdminReturnsEnvelope() throws Exception {
        BranchEntity entity = BranchEntity.builder()
                .id(1L)
                .name("Sede Centro")
                .address("Calle 1")
                .specialty("General")
                .active(true)
                .build();
        Page<BranchEntity> page = new PageImpl<>(List.of(entity));

        when(sedeService.list(anyInt(), anyInt(), nullable(String.class))).thenReturn(page);

        mvc.perform(get("/api/sedes")
                        .header("Authorization", ADMIN_TOKEN)
                        .header("X-Trace-Id", "trace-test-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.traceId").value("trace-test-1"))
                .andExpect(jsonPath("$.data.items[0].nombre").value("Sede Centro"))
                .andExpect(jsonPath("$.data.items[0].name").value("Sede Centro"));
    }

    @Test
        @DisplayName("POST /api/sedes invalido retorna 400 con envelope")
    void invalidCreateReturns400Envelope() throws Exception {
        String invalidPayload = "{\"nombre\":\"\",\"direccion\":\"\",\"especialidad\":\"\"}";

                mvc.perform(post("/api/sedes")
                        .header("Authorization", ADMIN_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Payload invalido"))
                .andExpect(jsonPath("$.data.nombre").exists());
    }
}
