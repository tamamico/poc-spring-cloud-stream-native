package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;

class WhiteboardGreetingValidator
        implements GreetingValidator {

    private final Whiteboard whiteboard;

    WhiteboardGreetingValidator(
            final Whiteboard whiteboard
    ) {
        this.whiteboard = whiteboard;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        final String message = this.whiteboard.read().getMessage().toString();
        assertions.accept(message);
    }
}
