package es.ecristobal.poc.scs;

import java.util.List;
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
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static org.springframework.kafka.support.KafkaHeaders.KEY;
import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Hooks.enableAutomaticContextPropagation;

@Slf4j
@Configuration
class StreamConfiguration {

    private static final List<String> FIELDS = List.of("user.name");

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
        FIELDS.forEach(field -> getInstance().registerThreadLocalAccessor(field, () -> MDC.get(field),
                                                                          username -> MDC.put(field, username),
                                                                          () -> MDC.remove(field)));
        return flux -> flux.contextWrite(context -> context.put("user.name", "Pirindolo"))
                           .doOnNext(input -> log.atInfo()
                                                 .setMessage("Greeting {}")
                                                 .addArgument(input.getPayload()
                                                                   .getWho())
                                                 .log())
                           .map(input -> {
                               ofNullable(input.getHeaders()
                                               .get(ACKNOWLEDGMENT, ReceiverOffset.class)).ifPresent(
                                       ReceiverOffset::acknowledge);
                               return withPayload(greeter.greet(input.getPayload())).setHeader(KEY, input.getHeaders()
                                                                                                         .get("kafka_receivedMessageKey"))
                                                                                    .build();
                           });
    }

}
