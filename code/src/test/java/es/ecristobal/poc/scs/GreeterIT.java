package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;

import static es.ecristobal.poc.scs.TestScenarios.greetOk;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;

@Testcontainers
@SpringBootTest
@SuppressWarnings("java:S2699")
class GreeterIT {

    private static final String DOCKER_IMAGE = "docker.redpanda.com/redpandadata/redpanda:v23.1.2";

    private static final String KAFKA_BROKER_USER     = "admin";
    private static final String KAFKA_BROKER_PASSWORD = "test";
    private static final String USER_SETUP            = "{\"username\": \"%s\", \"password\": \"%s\", \"algorithm\": \"%s\"}";

    private static final String SECURITY_PROTOCOL    = "SASL_PLAINTEXT";
    private static final String SASL_MECHANISM       = "SCRAM-SHA-256";
    private static final String JAAS_LOGIN_MODULE    = "org.apache.kafka.common.security.scram.ScramLoginModule";
    private static final String JAAS_CONFIG_TEMPLATE = "%s required username=\"%s\" " + "password=\"%s\";";

    private static final String INPUT_TOPIC_PATTERN = "^input\\.(?:men|women)\\.avro$";
    private static final String INPUT_TOPIC_MEN     = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN   = "input.women.avro";
    private static final String OUTPUT_TOPIC        = "output.avro";

    private static final Duration METADATA_MAX_AGE = ofSeconds(1);

    private static GreetingFactory greetingFactoryMen;
    private static GreetingFactory greetingFactoryWomen;

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
                     () -> format(JAAS_CONFIG_TEMPLATE, JAAS_LOGIN_MODULE, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD));
    }

    @BeforeAll
    static void setUp() {
        final String adminUrl = format("%s/v1/security/users", broker.getAdminAddress());
        given().contentType("application/json")
               .body(format(USER_SETUP, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD, SASL_MECHANISM))
               .post(adminUrl)
               .then()
               .statusCode(200);
        greetingFactoryMen   = KafkaGreetingFactory.newInstance()
                                                   .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                   .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                   .withInputTopic(INPUT_TOPIC_MEN)
                                                   .withOutputTopic(OUTPUT_TOPIC);
        greetingFactoryWomen = KafkaGreetingFactory.newInstance()
                                                   .withUrls(broker.getBootstrapServers(), broker.getSchemaRegistryAddress())
                                                   .withAuthentication(KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD)
                                                   .withInputTopic(INPUT_TOPIC_WOMEN)
                                                   .withOutputTopic(OUTPUT_TOPIC);
    }

    @Test
    void testGreetMen() {
        greetOk("Steve", greetingFactoryMen);
    }

    @Test
    void testGreetWomen() {
        greetOk("Laurene", greetingFactoryWomen);
    }

}
