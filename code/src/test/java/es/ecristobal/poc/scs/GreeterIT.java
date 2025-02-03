package es.ecristobal.poc.scs;

import javax.security.auth.spi.LoginModule;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitor;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import static java.lang.String.format;

import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaAuthentication;
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaUrls;
import static io.restassured.RestAssured.given;

@Testcontainers
@AutoConfigureObservability
@SpringBootTest
@SuppressWarnings("java:S2699")
class GreeterIT
        extends TestScenarios {

    private static final String DOCKER_IMAGE = "docker.redpanda.com/redpandadata/redpanda:v24.1.2";

    private static final Class<? extends LoginModule> KAFKA_LOGIN_MODULE = ScramLoginModule.class;
    private static final String                       KAFKA_USER         = "admin";
    private static final String                       KAFKA_PASSWORD     = "test";

    private static final String SASL_MECHANISM    = "SCRAM-SHA-256";
    private static final String SECURITY_PROTOCOL = "SASL_PLAINTEXT";

    private static final String INPUT_TOPIC_MEN   = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN = "input.women.avro";
    private static final String OUTPUT_TOPIC      = "output.avro";

    private static final String USER_SETUP = "{\"username\": \"%s\", \"password\": \"%s\", " + "\"algorithm\": \"%s\"}";

    @Container
    private static final RedpandaContainer broker = new RedpandaContainer(DOCKER_IMAGE).enableAuthorization()
                                                                                       .enableSasl()
                                                                                       .enableSchemaRegistryHttpBasicAuth()
                                                                                       .withSuperuser(KAFKA_USER);

    private static KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator                                greetingValidator;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Required properties
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url",
                     broker::getSchemaRegistryAddress);
        registry.add("kafka.user", () -> KAFKA_USER);
        registry.add("kafka.password", () -> KAFKA_PASSWORD);
        registry.add("schema-registry.user", () -> KAFKA_USER);
        registry.add("schema-registry.password", () -> KAFKA_PASSWORD);
        // Overridden properties
        registry.add("spring.cloud.stream.kafka.binder.producer-properties.auto.register.schemas", () -> "true");
        registry.add("spring.cloud.stream.kafka.bindings.greet-in-0.consumer.start-offset", () -> "earliest");
        registry.add("spring.cloud.stream.kafka.binder.configuration.sasl.mechanism", () -> SASL_MECHANISM);
        registry.add("spring.cloud.stream.kafka.binder.configuration.security.protocol", () -> SECURITY_PROTOCOL);
        registry.add("kafka.login.module", KAFKA_LOGIN_MODULE::getName);
    }

    @BeforeAll
    static void setUp() {
        final String adminUrl = format("%s/v1/security/users", broker.getAdminAddress());
        given().contentType("application/json")
               .body(format(USER_SETUP, KAFKA_USER, KAFKA_PASSWORD, SASL_MECHANISM))
               .post(adminUrl)
               .then()
               .statusCode(200);
        final KafkaUrls urls = KafkaUrls.builder()
                                        .broker(broker.getBootstrapServers())
                                        .schemaRegistry(broker.getSchemaRegistryAddress())
                                        .build();
        final KafkaAuthentication authentication = KafkaAuthentication.builder()
                                                                      .type(KafkaAuthentication.AuthenticationType.SCRAM)
                                                                      .kafkaUsername(KAFKA_USER)
                                                                      .kafkaPassword(KAFKA_PASSWORD)
                                                                      .schemaRegistryUsername(KAFKA_USER)
                                                                      .schemaRegistryPassword(KAFKA_PASSWORD)
                                                                      .build();
        final KafkaGreetingFactory greetingFactory = KafkaGreetingFactory.builder()
                                                                         .urls(urls)
                                                                         .authentication(authentication)
                                                                         .autoRegisterSchemas(true)
                                                                         .build();
        greetingVisitorBuilder = greetingFactory.greetingVisitorBuilder();
        greetingValidator      = greetingFactory.greetingValidatorBuilder()
                                                .topic(OUTPUT_TOPIC)
                                                .build();
    }

    @Test
    void testGreetMen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.topic(INPUT_TOPIC_MEN)
                                                                      .build();
        this.greetOk("Steve", greetingVisitor, greetingValidator);
    }

    @Test
    void testGreetWomen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.topic(INPUT_TOPIC_WOMEN)
                                                                      .build();
        this.greetOk("Laurene", greetingVisitor, greetingValidator);
    }

}
