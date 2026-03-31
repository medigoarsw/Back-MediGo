error id: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/MediGoApplication.java:org/springframework/boot/autoconfigure/SpringBootApplication#
file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/MediGoApplication.java
empty definition using pc, found symbol in pc: org/springframework/boot/autoconfigure/SpringBootApplication#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 137
uri: file:///D:/ander/Documents/SEMESTRE%207/ARSW/PROYECTO/Back-MediGo/src/main/java/edu/escuelaing/arsw/medigo/MediGoApplication.java
text:
```scala
package edu.escuelaing.arsw.medigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.@@SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class MediGoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediGoApplication.class, args);
	}

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/springframework/boot/autoconfigure/SpringBootApplication#