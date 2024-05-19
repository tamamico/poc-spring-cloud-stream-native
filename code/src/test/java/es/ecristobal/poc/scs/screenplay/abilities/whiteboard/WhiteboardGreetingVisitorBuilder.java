package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitorBuilder;

public class WhiteboardGreetingVisitorBuilder
        implements GreetingVisitorBuilder {

    private final Whiteboard whiteboard;

    private WhiteboardGreetingVisitorBuilder(final Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
    }

    @Override
    public GreetingVisitor build() {
        return new WhiteboardGreetingVisitor(this.whiteboard);
    }

    static WhiteboardGreetingVisitorBuilder withWhiteboard(final Whiteboard whiteboard) {
        return new WhiteboardGreetingVisitorBuilder(whiteboard);
    }
}
