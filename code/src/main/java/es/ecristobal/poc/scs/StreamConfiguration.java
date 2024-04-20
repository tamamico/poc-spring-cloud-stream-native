package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
class StreamConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamConfiguration.class);

    @Bean
    Function<Flux<Input>, Flux<Output>> sayHi() {
        return flux -> flux.doOnNext(name -> LOGGER.info("Received name: {}", name))
                           .map(message -> Output.newBuilder().setMessage("Hello, {}!".replace("{}", message.getName())).build());
    }

}
