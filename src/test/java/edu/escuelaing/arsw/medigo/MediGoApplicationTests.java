package edu.escuelaing.arsw.medigo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import edu.escuelaing.arsw.medigo.TestWebSocketConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebSocketConfig.class)
class MediGoApplicationTests {

    @MockBean
    StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads() {
        System.out.println("Contexto cargado correctamente");
    }

}