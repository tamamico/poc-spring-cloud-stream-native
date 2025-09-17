package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import org.springframework.cloud.stream.binder.test.OutputDestination;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBinderGreetingValidator
        implements GreetingValidator {

    private static final Pattern PATTERN = Pattern.compile(".*\"message\": \"(.*)\"}, .*");

    private final OutputDestination outputDestination;

    TestBinderGreetingValidator(final OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        final String response = this.outputDestination.receive()
                                                      .toString();
        final Matcher responseMatcher = PATTERN.matcher(response);
        assertTrue(responseMatcher.matches());
        assertions.accept(responseMatcher.group(1));
    }

}
