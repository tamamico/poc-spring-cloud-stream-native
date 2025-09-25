package es.ecristobal.poc.scs;

import java.util.Map;
import java.util.function.Function;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;

import static java.util.Optional.ofNullable;

import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static org.springframework.kafka.support.KafkaHeaders.KEY;
import static org.springframework.messaging.support.MessageBuilder.withPayload;

@Configuration
class StreamConfiguration {

    private static final String KAFKA_KEY = "kafka_receivedMessageKey";

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Message<Input>>, Flux<Message<Output>>> greet(
            final Greeter greeter
    ) {
        return flux -> flux.map(message -> {
            final Input input = message.getPayload();
            final Map<String, String> context = Map.of("user.name", input.getWho()
                                                                         .toString());
            try {
                context.forEach(MDC::put);
                ofNullable(message.getHeaders()
                                  .get(ACKNOWLEDGMENT, ReceiverOffset.class)).ifPresent(ReceiverOffset::acknowledge);
                return withPayload(greeter.greet(input)).setHeader(KEY, message.getHeaders()
                                                                               .get(KAFKA_KEY))
                                                        .build();
            } finally {
                context.keySet()
                       .forEach(MDC::remove);
            }
        });
    }

}
