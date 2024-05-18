package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.actors.Customer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class PocTestBase {

    private static final Pattern GREETING_PATTERN = compile("^Hello, ([A-Z]++)!$");

    protected static GreetingVisitor   greetingVisitor;
    protected static GreetingValidator greetingValidator;

    protected void testGreetOk(final String customerName) {
        final Customer customer = new Customer(customerName);
        customer.accept(greetingVisitor);
        greetingValidator.with(message -> {
            final Matcher matcher = GREETING_PATTERN.matcher(message);
            assertTrue(matcher.matches());
            assertEquals(customer.name().toUpperCase(), matcher.group(1));
        });
    }
}
