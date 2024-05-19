package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;

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
    public GreetingVisitor greetingVisitor() {
        return new WhiteboardGreetingVisitor(this.whiteboard);
    }

    @Override
    public GreetingValidator greetingValidator() {
        return new WhiteboardGreetingValidator(this.whiteboard);
    }
}
