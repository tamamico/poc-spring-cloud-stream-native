package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;

import static java.lang.String.format;

class Greeter {

    Output greet(
            final Input input
    ) {
        return Output.newBuilder().setMessage(format("Hello, %S!", input.getName())).build();
    }
}
