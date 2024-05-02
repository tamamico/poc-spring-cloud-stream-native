package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.actors.Customer;
import es.ecristobal.poc.scs.screenplay.interactions.receive.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.interactions.send.GreetingVisitor;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("java:S2187") // Needed to remove SonarCloud false positive
abstract class ApplicationBaseTests {

    private static final Pattern GREETING_PATTERN = compile("^Hello, ([A-Z]++)!$");

    protected static GreetingVisitor   greetingVisitor;
    protected static GreetingValidator greetingValidator;

    @Test
    void testGreetOk() {
        final Customer customer = new Customer("Steve");
        customer.accept(greetingVisitor);
        greetingValidator.with(message -> {
            final Matcher matcher = GREETING_PATTERN.matcher(message);
            assertTrue(matcher.matches());
            assertEquals(customer.name().toUpperCase(), matcher.group(1));
        });
    }
}
