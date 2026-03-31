error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/MediGoApplicationTests.java:org/springframework/test/context/ActiveProfiles#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/MediGoApplicationTests.java
empty definition using pc, found symbol in pc: org/springframework/test/context/ActiveProfiles#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 232
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/test/java/edu/escuelaing/arsw/medigo/MediGoApplicationTests.java
text:
```scala
package edu.escuelaing.arsw.medigo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.@@ActiveProfiles;

import edu.escuelaing.arsw.medigo.TestWebSocketConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestWebSocketConfig.class)
class MediGoApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("Contexto cargado correctamente");
    }

}
```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/test/context/ActiveProfiles#