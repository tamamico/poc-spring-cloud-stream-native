package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.observability.micrometer.Micrometer.observation;

@Configuration
class StreamConfiguration {

    private static final Logger LOGGER = getLogger(StreamConfiguration.class);

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Flux<ConsumerRecord<String, Input>>>, Flux<Output>> greet(
            final Greeter greeter,
            final ObservationRegistry registry
    ) {
        return flux -> flux.flatMap(inner -> inner.map(ConsumerRecord::value)
                                                  .doOnNext(input -> LOGGER.info("Greeting {}", input.getName()))
                                                  .map(greeter::greet)
                                                  .tap(observation(registry)));
    }

}
