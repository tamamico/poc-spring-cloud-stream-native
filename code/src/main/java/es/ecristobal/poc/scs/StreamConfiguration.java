package es.ecristobal.poc.scs;

import java.util.function.Function;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.ReceiverOffset;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.kafka.support.KafkaHeaders.ACKNOWLEDGMENT;
import static reactor.core.observability.micrometer.Micrometer.observation;

@Configuration
class StreamConfiguration {

    private static final Logger LOGGER = getLogger(StreamConfiguration.class);

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Message<Input>>, Flux<Message<Output>>> greet(
            final Greeter greeter,
            final ObservationRegistry registry
    ) {
        return flux -> flux.doOnNext(input -> LOGGER.atInfo()
                                                    .setMessage("Greeting {}")
                                                    .addArgument(input.getPayload()
                                                                      .getWho())
                                                    .log())
                           .doOnNext(message -> message.getHeaders()
                                                       .get(ACKNOWLEDGMENT, ReceiverOffset.class)
                                                       .acknowledge())
                           .map(input -> this.buildMessageFrom(input, greeter))
                           .tap(observation(registry));
    }

    private Message<Output> buildMessageFrom(final Message<Input> input, final Greeter greeter) {
        return MessageBuilder.withPayload(greeter.greet(input.getPayload()))
                             .copyHeaders(input.getHeaders())
                             .build();
    }

}
