package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;

public class WhiteboardGreetingValidator
        implements GreetingValidator {

    private final Whiteboard whiteboard;

    @Builder
    WhiteboardGreetingValidator(
            final Whiteboard whiteboard
    ) {
        this.whiteboard = whiteboard;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        final String message = this.whiteboard.read()
                                              .getMessage()
                                              .toString();
        assertions.accept(message);
    }

}
