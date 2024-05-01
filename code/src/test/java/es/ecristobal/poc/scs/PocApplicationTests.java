package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.actors.Customer;
import es.ecristobal.poc.scs.screenplay.interactions.ReceiveGreeting;
import es.ecristobal.poc.scs.screenplay.interactions.SendGreeting;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class PocApplicationTests {

    private static final String INPUT_TOPIC_NAME  = "input-topic";
    private static final String OUTPUT_TOPIC_NAME = "output-topic";

    private static final Pattern GREETING_PATTERN = Pattern.compile("^Hello, ([A-Z]++)!$");

    @Container
    private static RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    private static SendGreeting    sendGreeting;
    private static ReceiveGreeting receiveGreeting;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.bindings.greeter-in-0.destination", () -> INPUT_TOPIC_NAME);
        registry.add("spring.cloud.stream.bindings.greeter-out-0.destination", () -> OUTPUT_TOPIC_NAME);
    }

    @BeforeAll
    static void setUp() {
        sendGreeting    = new SendGreeting(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(), INPUT_TOPIC_NAME);
        receiveGreeting = new ReceiveGreeting(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(), OUTPUT_TOPIC_NAME);
    }

    @Test
    void testSayHi() {
        final Customer customer = new Customer("Steve");
        sendGreeting.to(customer);
        receiveGreeting.check(message -> {
            final Matcher matcher = GREETING_PATTERN.matcher(message);
            assertTrue(matcher.matches());
            assertEquals(customer.name().toUpperCase(), matcher.group(1));
        });
    }

}
