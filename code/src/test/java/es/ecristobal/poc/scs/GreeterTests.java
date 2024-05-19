package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.whiteboard.WhiteboardGreetingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static es.ecristobal.poc.scs.TestScenarios.greetOk;

@SuppressWarnings("java:S2699")
class GreeterTests {

    private static GreetingFactory greetingFactory;

    @BeforeAll
    static void setUp() {
        final Greeter greeter = new Greeter();
        greetingFactory = WhiteboardGreetingFactory.newInstance().withFunction(greeter::greet);
    }

    @Test
    void testGreet() {
        greetOk("Woz", greetingFactory);
    }
}
