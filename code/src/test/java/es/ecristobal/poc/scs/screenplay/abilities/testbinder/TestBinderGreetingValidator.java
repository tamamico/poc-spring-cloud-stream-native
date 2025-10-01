package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;
import org.springframework.cloud.stream.binder.test.OutputDestination;

public class TestBinderGreetingValidator
        implements GreetingValidator {

    private final OutputDestination outputDestination;

    @Builder
    private TestBinderGreetingValidator(final OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        assertions.accept(new String(this.outputDestination.receive()
                                                           .getPayload()));

    }

}
