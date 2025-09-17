package es.ecristobal.poc.scs;

import java.util.function.Function;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;

import static java.util.Optional.ofNullable;

import static reactor.core.observability.micrometer.Micrometer.observation;

@Slf4j
@Configuration
class StreamConfiguration {

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Message<Input>>, Flux<Message<Output>>> greet(
            final Greeter greeter,
            final ObservationRegistry registry
    ) {
        return flux -> flux.doOnNext(input -> log.atInfo()
                                                 .setMessage("Greeting {}")
                                                 .addArgument(input.getPayload()
                                                                   .getWho())
                                                 .log())
                           .map(input -> {
                               ofNullable(input.getHeaders()
                                               .get(KafkaHeaders.ACKNOWLEDGMENT, ReceiverOffset.class))
                               .ifPresent(ReceiverOffset::acknowledge);
                               return MessageBuilder.withPayload(greeter.greet(input.getPayload()))
                                                    .setHeader(KafkaHeaders.KEY, input.getHeaders()
                                                                                      .get("kafka_receivedMessageKey"))
                                                    .build();
                           })
                           .tap(observation(registry));
    }

}
