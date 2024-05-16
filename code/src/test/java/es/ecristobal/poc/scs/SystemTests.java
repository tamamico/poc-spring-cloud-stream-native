package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.actors.Customer;
import es.ecristobal.poc.scs.screenplay.interactions.receive.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.interactions.receive.GreetingValidatorBuilder;
import es.ecristobal.poc.scs.screenplay.interactions.send.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.interactions.send.GreetingVisitorBuilder;
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

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;
import static java.util.regex.Pattern.compile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
class SystemTests {

    private static final String INPUT_TOPIC_PATTERN = "^input\\.(?:men|women)\\.avro$";
    private static final String INPUT_TOPIC_MEN     = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN   = "input.women.avro";
    private static final String OUTPUT_TOPIC        = "output.avro";

    private static final String SECURITY_PROTOCOL    = "SASL_PLAINTEXT";
    private static final String SASL_MECHANISM       = "SCRAM-SHA-256";
    private static final String JAAS_CONFIG_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" " +
                                                       "password=\"%s\";";

    private static final String KAFKA_BROKER_USER     = "admin";
    private static final String KAFKA_BROKER_PASSWORD = "test";

    private static final Duration METADATA_MAX_AGE = ofSeconds(1);

    private static final Pattern GREETING_PATTERN = compile("^Hello, ([A-Z]++)!$");

    private static GreetingVisitor   menGreetingVisitor;
    private static GreetingVisitor   womenGreetingVisitor;
    private static GreetingValidator greetingValidator;

    @Container
    private static RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2")
            .enableAuthorization()
            .enableSasl()
            .withSuperuser(KAFKA_BROKER_USER);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms", METADATA_MAX_AGE::toMillis);
        registry.add("spring.cloud.stream.bindings.greeter-in-0.destination", () -> INPUT_TOPIC_PATTERN);
        registry.add("spring.cloud.stream.bindings.greeter-out-0.destination", () -> OUTPUT_TOPIC);
        registry.add("spring.cloud.stream.kafka.binder.configuration.security.protocol", () -> SECURITY_PROTOCOL);
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.mechanism", () -> SASL_MECHANISM);
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.jaas.config",
                     () -> format(JAAS_CONFIG_TEMPLATE, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD));
    }

    @BeforeAll
    static void setUp() {
        final String adminUrl = format("%s/v1/security/users", broker.getAdminAddress());
        given().contentType("application/json")
               .body(format("{\"username\": \"%s\", \"password\": \"%s\", \"algorithm\": \"%s\"}", KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD,
                            SASL_MECHANISM))
               .post(adminUrl)
               .then()
               .statusCode(200);
        menGreetingVisitor   = GreetingVisitorBuilder.withKafka()
                                                     .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                     .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                     .build(INPUT_TOPIC_MEN);
        womenGreetingVisitor = GreetingVisitorBuilder.withKafka()
                                                     .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                     .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                     .build(INPUT_TOPIC_WOMEN);
        greetingValidator    = GreetingValidatorBuilder.withKafka()
                                                       .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                       .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                       .build(OUTPUT_TOPIC);
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
