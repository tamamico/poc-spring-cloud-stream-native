package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.util.function.Function;

@Configuration
class StreamConfiguration {

    @Bean
    Function<Flux<Input>, Flux<Output>> sayHi() {
        return flux -> flux.log()
                .map(message -> Output.newBuilder().setMessage("Hello, {}!".replace("{}", message.getName())).build());
    }

}
