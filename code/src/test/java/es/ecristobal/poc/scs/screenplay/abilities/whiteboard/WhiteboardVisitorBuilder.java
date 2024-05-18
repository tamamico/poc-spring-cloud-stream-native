package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;

public class WhiteboardVisitorBuilder {

    private Whiteboard whiteboard;

    private WhiteboardVisitorBuilder() {
        // Empty constructor
    }

    public static WhiteboardVisitorBuilder newInstance() {
        return new WhiteboardVisitorBuilder();
    }

    public WhiteboardVisitorBuilder withWhiteboard(Whiteboard whiteboard) {
        this.whiteboard = whiteboard;
        return this;
    }

    public GreetingVisitor build() {
        return new WhiteboardGreetingVisitor(this.whiteboard);
    }
}
