package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.testbinder.TestBinderGreetingFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;

@SpringBootTest
@EnableTestBinder
@SuppressWarnings("java:S2699")
class GreeterTestBinderTests
        extends TestScenarios {

    @Autowired
    private InputDestination input;

    @Autowired
    private OutputDestination output;

    @Test
    void testGreetMen() {
        final TestBinderGreetingFactory greetingFactory = TestBinderGreetingFactory.builder()
                                                                                   .inputDestination(input)
                                                                                   .outputDestination(output)
                                                                                   .build();
        final GreetingVisitor   greetingVisitor   = greetingFactory.greetingVisitor();
        final GreetingValidator greetingValidator = greetingFactory.greetingValidator();
        this.greetOk("Steve", greetingVisitor, greetingValidator);
    }

    @Test
    void testGreetWomen() {
        final TestBinderGreetingFactory greetingFactory = TestBinderGreetingFactory.builder()
                                                                                   .inputDestination(input)
                                                                                   .outputDestination(output)
                                                                                   .build();
        final GreetingVisitor   greetingVisitor   = greetingFactory.greetingVisitor();
        final GreetingValidator greetingValidator = greetingFactory.greetingValidator();
        this.greetOk("Laurene", greetingVisitor, greetingValidator);
    }

}
