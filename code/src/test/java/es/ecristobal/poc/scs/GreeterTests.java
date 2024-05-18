package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.Whiteboard;
import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.WhiteboardValidatorBuilder;
import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.WhiteboardVisitorBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GreeterTests
        extends PocTestBase {

    @BeforeAll
    static void setUp() {
        final Greeter    greeter    = new Greeter();
        final Whiteboard whiteboard = new Whiteboard(greeter::greet);
        greetingVisitor   = WhiteboardVisitorBuilder.newInstance().withWhiteboard(whiteboard).build();
        greetingValidator = WhiteboardValidatorBuilder.newInstance().withWhiteboard(whiteboard).build();
    }

    @Test
    void testGreet() {
        this.testGreetOk("Woz");
    }
}
