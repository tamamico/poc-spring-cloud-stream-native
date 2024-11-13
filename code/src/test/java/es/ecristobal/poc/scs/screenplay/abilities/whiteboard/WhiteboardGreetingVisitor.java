package es.ecristobal.poc.scs.screenplay.abilities.whiteboard;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import lombok.Builder;

public class WhiteboardGreetingVisitor
        extends GreetingVisitor {

    private final Whiteboard whiteboard;

    @Builder
    WhiteboardGreetingVisitor(
            final Whiteboard whiteboard
    ) {
        this.whiteboard = whiteboard;
    }

    @Override
    public void visit(final Customer customer) {
        this.whiteboard.write(this.buildFrom(customer));
    }

}
