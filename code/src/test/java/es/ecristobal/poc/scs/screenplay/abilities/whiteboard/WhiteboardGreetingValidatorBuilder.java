package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidatorBuilder;

public class WhiteboardGreetingValidatorBuilder
        implements GreetingValidatorBuilder {

    private final Whiteboard whiteboard;

    private WhiteboardGreetingValidatorBuilder(final Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
    }

    @Override
    public GreetingValidator build() {
        return new WhiteboardGreetingValidator(this.whiteboard);
    }

    static WhiteboardGreetingValidatorBuilder withWhiteboard(final Whiteboard whiteboard) {
        return new WhiteboardGreetingValidatorBuilder(whiteboard);
    }
}
