package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreeterTests {

    private static final Pattern GREETING_PATTERN = compile("^Hello, ([A-Z]++)!$");

    private final Greeter greeter = new Greeter();

    @Test
    void testGreet() {
        final String  customerName = "Woz";
        final Output  output       = greeter.greet(Input.newBuilder().setName(customerName).build());
        final String  message      = output.getMessage().toString();
        final Matcher matcher      = GREETING_PATTERN.matcher(message);
        assertTrue(matcher.matches());
        assertEquals(customerName.toUpperCase(), matcher.group(1));
    }
}
