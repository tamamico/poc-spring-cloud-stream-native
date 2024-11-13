package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import java.util.function.Function;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;

class Whiteboard {

    private final Function<Input, Output> function;

    private Output output;

    public Whiteboard(Function<Input, Output> function) {
        this.function = function;
    }

    public void write(final Input input) {
        this.output = this.function.apply(input);
    }

    public Output read() {
        return this.output;
    }

}
