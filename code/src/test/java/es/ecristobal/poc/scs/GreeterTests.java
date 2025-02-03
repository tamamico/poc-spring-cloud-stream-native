package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.WhiteboardGreetingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2699")
class GreeterTests
        extends TestScenarios {

    private static GreetingVisitor   greetingVisitor;
    private static GreetingValidator greetingValidator;

    @BeforeAll
    static void setUp() {
        //@formatter:off
        final WhiteboardGreetingFactory greetingFactory = WhiteboardGreetingFactory.builder()
                .function(input -> new Greeter().greet(input)).build();
        //@formatter:on
        greetingVisitor   = greetingFactory.greetingVisitorBuilder()
                                           .build();
        greetingValidator = greetingFactory.greetingValidatorBuilder()
                                           .build();
    }

    @Test
    void testGreet() {
        this.greetOk("Woz", greetingVisitor, greetingValidator);
    }

}
