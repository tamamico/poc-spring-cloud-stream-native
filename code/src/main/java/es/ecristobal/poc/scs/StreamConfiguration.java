package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    Function<Flux<Flux<ConsumerRecord<String, Input>>>, Flux<Output>> greeter(final ObservationRegistry registry) {
        return flux -> flux.flatMap(inner -> inner.map(ConsumerRecord::value)
                                                  .doOnNext(input -> LOGGER.info("Greeting {}", input.getName()))
                                                  .map(this::greeter)
                                                  .tap(Micrometer.observation(registry)));
    }

    private Output greeter(final Input input) {
        return Output.newBuilder().setMessage(String.format("Hello, %S!", input.getName())).build();
    }

}
