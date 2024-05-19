package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingFactory;

import java.util.function.Function;

public class WhiteboardGreetingFactory
        implements GreetingFactory {

    private Whiteboard whiteboard;

    private WhiteboardGreetingFactory() {
        // Empty constructor
    }

    public static WhiteboardGreetingFactory newInstance() {
        return new WhiteboardGreetingFactory();
    }

    public WhiteboardGreetingFactory withFunction(final Function<Input, Output> function) {
        this.whiteboard = new Whiteboard(function);
        return this;
    }

    @Override
    public WhiteboardGreetingVisitorBuilder greetingVisitorBuilder() {
        return WhiteboardGreetingVisitorBuilder.withWhiteboard(this.whiteboard);
    }

    @Override
    public WhiteboardGreetingValidatorBuilder greetingValidatorBuilder() {
        return WhiteboardGreetingValidatorBuilder.withWhiteboard(this.whiteboard);
    }
}
