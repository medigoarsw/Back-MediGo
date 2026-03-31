package edu.escuelaing.arsw.MediGo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import edu.escuelaing.arsw.MediGo.TestWebSocketConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebSocketConfig.class)
class MediGoApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("Contexto cargado correctamente");
    }

}