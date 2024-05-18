package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;

public class WhiteboardValidatorBuilder {

    private Whiteboard whiteboard;

    private WhiteboardValidatorBuilder() {
        // Empty constructor
    }

    public static WhiteboardValidatorBuilder newInstance() {
        return new WhiteboardValidatorBuilder();
    }

    public WhiteboardValidatorBuilder withWhiteboard(final Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
        return this;
    }

    public GreetingValidator build() {
        return new WhiteboardGreetingValidator(this.whiteboard);
    }
}
