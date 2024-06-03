package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import lombok.Builder;

import java.util.function.Function;

public class WhiteboardGreetingFactory {

    private final Whiteboard whiteboard;

    @Builder
    private WhiteboardGreetingFactory(final Function<Input, Output> function) {
        this.whiteboard = new Whiteboard(function);
    }

    public WhiteboardGreetingVisitor.WhiteboardGreetingVisitorBuilder greetingVisitorBuilder() {
        return WhiteboardGreetingVisitor.builder().whiteboard(this.whiteboard);
    }

    public WhiteboardGreetingValidator.WhiteboardGreetingValidatorBuilder greetingValidatorBuilder() {
        return WhiteboardGreetingValidator.builder().whiteboard(this.whiteboard);
    }
}
