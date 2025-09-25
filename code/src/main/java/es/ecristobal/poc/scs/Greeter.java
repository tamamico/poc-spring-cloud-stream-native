package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

@Slf4j
class Greeter {

    Output greet(
            final Input input
    ) {
        log.atInfo()
           .setMessage("Received person to greet")
           .log();
        return Output.newBuilder()
                     .setMessage(format("Hello, %S!", input.getWho()))
                     .build();
    }

}
