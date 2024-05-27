package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitorBuilder;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import javax.security.auth.spi.LoginModule;
import java.time.Duration;

import static es.ecristobal.poc.scs.TestScenarios.greetOk;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;

@Testcontainers
@SpringBootTest
@SuppressWarnings("java:S2699")
class GreeterIT {

    private static final String DOCKER_IMAGE = "docker.redpanda.com/redpandadata/redpanda:v24.1.2";

    private static final String KAFKA_BROKER_USER     = "admin";
    private static final String KAFKA_BROKER_PASSWORD = "test";

    private static final String                       SASL_MECHANISM    = "SCRAM-SHA-256";
    private static final Class<? extends LoginModule> LOGIN_MODULE      = ScramLoginModule.class;

    private static final String USER_SETUP = "{\"username\": \"%s\", \"password\": \"%s\", \"algorithm\": \"%s\"}";

    private static final String INPUT_TOPIC_PATTERN = "^input\\.(?:men|women)\\.avro$";
    private static final String INPUT_TOPIC_MEN     = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN   = "input.women.avro";
    private static final String OUTPUT_TOPIC        = "output.avro";

    private static final Duration METADATA_MAX_AGE = ofSeconds(1);

    private static KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator           greetingValidator;

    @Container
    private static RedpandaContainer broker = new RedpandaContainer(DOCKER_IMAGE).enableAuthorization()
                                                                                 .enableSasl()
                                                                                 .withSuperuser(KAFKA_BROKER_USER);

    @Test
    void testGreetMen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.withTopic(INPUT_TOPIC_MEN).build();
        greetOk("Steve", greetingVisitor, greetingValidator);
    }

    @Test
    void testGreetWomen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.withTopic(INPUT_TOPIC_WOMEN).build();
        greetOk("Laurene", greetingVisitor, greetingValidator);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.bindings.greet-in-0.destination", () -> INPUT_TOPIC_PATTERN);
        registry.add("spring.cloud.stream.bindings.greet-out-0.destination", () -> OUTPUT_TOPIC);
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms", METADATA_MAX_AGE::toMillis);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.mechanism", () -> SASL_MECHANISM);
        registry.add("jaas.login.module", LOGIN_MODULE::getName);
        registry.add("kafka.broker.user", () -> KAFKA_BROKER_USER);
        registry.add("kafka.broker.password", () -> KAFKA_BROKER_PASSWORD);
    }

    @BeforeAll
    static void setUp() {
        final String adminUrl = format("%s/v1/security/users", broker.getAdminAddress());
        given().contentType("application/json")
               .body(format(USER_SETUP, KAFKA_BROKER_USER, KAFKA_BROKER_PASSWORD, SASL_MECHANISM))
               .post(adminUrl)
               .then()
               .statusCode(200);
        final KafkaGreetingFactory greetingFactory = KafkaGreetingFactory.newInstance()
                                                                         .withUrls(broker.getBootstrapServers(),
                                                                                   broker.getSchemaRegistryAddress())
                                                                         .withAuthentication(LOGIN_MODULE, KAFKA_BROKER_USER,
                                                                                             KAFKA_BROKER_PASSWORD);
        greetingVisitorBuilder = greetingFactory.greetingVisitorBuilder();
        greetingValidator      = greetingFactory.greetingValidatorBuilder().withTopic(OUTPUT_TOPIC).build();
    }

}
