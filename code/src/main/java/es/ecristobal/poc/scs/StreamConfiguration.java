package es.ecristobal.poc.scs;

import java.util.function.Function;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;

import static java.util.Optional.ofNullable;

import static io.micrometer.context.ContextRegistry.getInstance;
import static org.springframework.integration.support.MessageBuilder.withPayload;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static org.springframework.kafka.support.KafkaHeaders.KEY;
import static reactor.core.publisher.Hooks.enableAutomaticContextPropagation;
import static reactor.util.context.Context.of;

@Slf4j
@Configuration
class StreamConfiguration {

    private static final String KAFKA_KEY = "kafka_receivedMessageKey";
    private static final String USER_NAME = "user.name";

    @PostConstruct
    public void init() {
        enableAutomaticContextPropagation();
    }

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Message<Input>>, Flux<Message<Output>>> greet(
            final Greeter greeter
    ) {
        getInstance().registerThreadLocalAccessor(USER_NAME, () -> MDC.get(USER_NAME),
                                                  username -> MDC.put(USER_NAME, username),
                                                  () -> MDC.remove(USER_NAME));
        return flux -> flux.flatMap(message -> {
            final Input input = message.getPayload();
            ofNullable(message.getHeaders()
                              .get(ACKNOWLEDGMENT, ReceiverOffset.class)).ifPresent(ReceiverOffset::acknowledge);
            return greeter.greet(input)
                          .contextWrite(of(USER_NAME, input.getWho()
                                                           .toString()))
                          .map(output -> withPayload(output).setHeader(KEY, message.getHeaders()
                                                                                   .get(KAFKA_KEY))
                                                            .build());

        });
    }

}
