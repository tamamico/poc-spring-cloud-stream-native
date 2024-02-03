package es.ecristobal.poc.scs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@SpringBootApplication
public class ScsNativeApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScsNativeApplication.class);

    @Bean
    Function<Flux<String>, Flux<String>> sayHi() {
        return flux -> flux.doOnNext(name -> LOGGER.info("Received name: {}", name)).map("Hello, "::concat);
    }

    public static void main(String[] args) {
        SpringApplication.run(ScsNativeApplication.class, args);
    }

}
