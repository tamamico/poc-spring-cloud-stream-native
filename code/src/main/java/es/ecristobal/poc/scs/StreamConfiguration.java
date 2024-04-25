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

@Configuration
class StreamConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConfiguration.class);

    @Bean
    Function<Flux<Input>, Flux<Output>> sayHi(final ObservationRegistry registry) {
        return flux -> flux.doOnNext(input -> LOGGER.info("Received name: {}", input.getName()))
                           .map(input -> Output.newBuilder().setMessage("Hello, {}!".replace("{}", input.getName())).build())
                           .tap(Micrometer.observation(registry));
    }

}
