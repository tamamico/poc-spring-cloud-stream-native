package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import static java.lang.String.format;

import static es.ecristobal.poc.scs.avro.Output.newBuilder;
import static reactor.core.publisher.Mono.just;

@Slf4j
class Greeter {

    Mono<Output> greet(
            final Input input
    ) {
        return just(input.getWho()).doOnNext(name -> log.info("Received person name to greet"))
                                   .map(name -> newBuilder().setMessage(format("Hello, %S!", name))
                                                            .build());
    }

}
