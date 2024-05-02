package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.observability.micrometer.Micrometer;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static java.lang.String.format;

@Configuration
class SpringConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfiguration.class);

    @Bean
    Function<Flux<Input>, Flux<Output>> greeter(final ObservationRegistry registry) {
        return flux -> flux.doOnNext(input -> LOGGER.info("Greeting {}", input.getName()))
                           .map(this.greeter())
                           .tap(Micrometer.observation(registry));
    }

    private Function<Input, Output> greeter() {
        return input -> Output.newBuilder().setMessage(format("Hello, %S!", input.getName())).build();
    }

}
