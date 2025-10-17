package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.testbinder.TestBinderGreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.testbinder.TestBinderGreetingVisitor;
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

    private final GreetingVisitor   greetingVisitor;
    private final GreetingValidator greetingValidator;

    @Autowired
    GreeterTestBinderTests(final InputDestination input, final OutputDestination output) {
        this.greetingVisitor   = TestBinderGreetingVisitor.builder()
                                                          .inputDestination(input)
                                                          .build();
        this.greetingValidator = TestBinderGreetingValidator.builder()
                                                            .outputDestination(output)
                                                            .build();
    }

    @Test
    void testGreetMen() {
        this.greetOk("Steve", this.greetingVisitor, this.greetingValidator);
    }

    @Test
    void testGreetWomen() {
        this.greetOk("Laurene", this.greetingVisitor, this.greetingValidator);
    }

}
