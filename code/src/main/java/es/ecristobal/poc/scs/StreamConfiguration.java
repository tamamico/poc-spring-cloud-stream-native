package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
    Function<Flux<Flux<ConsumerRecord<String, Input>>>, Flux<Message<Output>>> greet(
            final Greeter greeter,
            final ObservationRegistry registry
    ) {
        return outer -> outer.flatMap(inner -> inner.doOnNext(input -> LOGGER.info("Greeting {}", input.value().getName()))
                                                    .map(input -> MessageBuilder.withPayload(greeter.greet(input.value()))
                                                                                .setHeader("kafka_messageKey", input.key())
                                                                                .build())
                                                    .tap(observation(registry)));
    }

}
