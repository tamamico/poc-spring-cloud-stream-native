package es.ecristobal.poc.scs;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;
import static reactor.core.publisher.Hooks.enableAutomaticContextPropagation;

@SpringBootApplication
public class GreeterApplication {

    public static void main(String[] args) {
        enableAutomaticContextPropagation();
        run(GreeterApplication.class, args);
    }

}
