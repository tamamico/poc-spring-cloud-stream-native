package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.actors.Customer;
import es.ecristobal.poc.scs.screenplay.interactions.receive.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.interactions.receive.KafkaBinderGreetingValidator;
import es.ecristobal.poc.scs.screenplay.interactions.send.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.interactions.send.KafkaBinderGreetingVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class SystemTests {

    private static final String INPUT_TOPIC_PATTERN = "^input\\.(?:men|women)\\.avro$";
    private static final String INPUT_TOPIC_MEN     = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN   = "input.women.avro";
    private static final String OUTPUT_TOPIC        = "output.avro";

    private static final Duration METADATA_MAX_AGE = Duration.ofSeconds(1);

    private static final Pattern GREETING_PATTERN = Pattern.compile("^Hello, ([A-Z]++)!$");

    private static GreetingVisitor   menGreetingVisitor;
    private static GreetingVisitor   womenGreetingVisitor;
    private static GreetingValidator greetingValidator;

    @Container
    private static RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms", METADATA_MAX_AGE::toMillis);
        registry.add("spring.cloud.stream.bindings.greeter-in-0.destination", () -> INPUT_TOPIC_PATTERN);
        registry.add("spring.cloud.stream.bindings.greeter-out-0.destination", () -> OUTPUT_TOPIC);
    }

    @BeforeAll
    static void setUp() {
        menGreetingVisitor   = new KafkaBinderGreetingVisitor(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(),
                                                              INPUT_TOPIC_MEN);
        womenGreetingVisitor = new KafkaBinderGreetingVisitor(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(),
                                                              INPUT_TOPIC_WOMEN);
        greetingValidator    = new KafkaBinderGreetingValidator(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(),
                                                                OUTPUT_TOPIC);
    }

    @Test
    void testGreetingMen() {
        this.testGreetOk("Steve", menGreetingVisitor);
    }

    @Test
    void testGreetingWomen() {
        this.testGreetOk("Laurene", womenGreetingVisitor);
    }

    private void testGreetOk(
            final String customerName,
            final GreetingVisitor greetingVisitor
    ) {
        final Customer customer = new Customer(customerName);
        customer.accept(greetingVisitor);
        greetingValidator.with(message -> {
            final Matcher matcher = GREETING_PATTERN.matcher(message);
            assertTrue(matcher.matches());
            assertEquals(customer.name().toUpperCase(), matcher.group(1));
        });
    }

}
