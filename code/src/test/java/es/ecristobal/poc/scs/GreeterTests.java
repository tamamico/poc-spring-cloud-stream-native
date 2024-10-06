package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.WhiteboardGreetingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static es.ecristobal.poc.scs.TestScenarios.greetOk;

@SuppressWarnings("java:S2699")
class GreeterTests {

    private static GreetingVisitor   greetingVisitor;
    private static GreetingValidator greetingValidator;

    @BeforeAll
    static void setUp() {
        final WhiteboardGreetingFactory greetingFactory = WhiteboardGreetingFactory.builder()
                                                                                   .function(input -> new Greeter().greet(input))
                                                                                   .build();
        greetingVisitor   = greetingFactory.greetingVisitorBuilder().build();
        greetingValidator = greetingFactory.greetingValidatorBuilder().build();
    }

    @Test
    void testGreet() {
        greetOk("Woz", greetingVisitor, greetingValidator);
    }
}
