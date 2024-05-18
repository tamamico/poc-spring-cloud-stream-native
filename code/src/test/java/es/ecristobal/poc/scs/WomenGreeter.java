package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingValidatorBuilder;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitorBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;

@Testcontainers
@SpringBootTest
@SuppressWarnings("java:S3577")
class WomenGreeter
        extends PocTestBase {

    private static final String DOCKER_IMAGE = "docker.redpanda.com/redpandadata/redpanda:v23.1.2";
    private static final String USER_SETUP   = "{\"username\": \"%s\", \"password\": \"%s\", \"algorithm\": \"%s\"}";

    private static final String KAFKA_BROKER_USER     = "admin";
    private static final String KAFKA_BROKER_PASSWORD = "test";

    private static final String SECURITY_PROTOCOL    = "SASL_PLAINTEXT";
    private static final String SASL_MECHANISM       = "SCRAM-SHA-256";
    private static final String JAAS_CONFIG_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" " +
                                                       "password=\"%s\";";

    private static final String INPUT_TOPIC_PATTERN = "^input\\.(?:men|women)\\.avro$";
    private static final String INPUT_TOPIC         = "input.women.avro";
    private static final String OUTPUT_TOPIC        = "output.avro";

    private static final Duration METADATA_MAX_AGE = ofSeconds(1);

    @Container
    private static RedpandaContainer broker = new RedpandaContainer(DOCKER_IMAGE).enableAuthorization()
                                                                                 .enableSasl()
                                                                                 .withSuperuser(KAFKA_BROKER_USER);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.bindings.greet-in-0.destination", () -> INPUT_TOPIC_PATTERN);
        registry.add("spring.cloud.stream.bindings.greet-out-0.destination", () -> OUTPUT_TOPIC);
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms", METADATA_MAX_AGE::toMillis);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.kafka.binder.configuration.security.protocol", () -> SECURITY_PROTOCOL);
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.mechanism", () -> SASL_MECHANISM);
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.jaas.config",
                     () -> format(JAAS_CONFIG_TEMPLATE, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD));
    }

    @BeforeAll
    static void setUp() {
        // Set-up Redpanda authentication
        final String adminUrl = format("%s/v1/security/users", broker.getAdminAddress());
        given().contentType("application/json")
               .body(format(USER_SETUP, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD, SASL_MECHANISM))
               .post(adminUrl)
               .then()
               .statusCode(200);
        // Instantiate screenplay interaction objects
        greetingVisitor   = KafkaGreetingVisitorBuilder.newInstance()
                                                       .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                       .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                       .build(INPUT_TOPIC);
        greetingValidator = KafkaGreetingValidatorBuilder.newInstance()
                                                         .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                         .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                         .build(OUTPUT_TOPIC);
    }

    @Test
    void testGreetWomen() {
        this.testGreetOk("Steve");
    }

}
