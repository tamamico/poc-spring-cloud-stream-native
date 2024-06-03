package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitor;
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
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaAuthentication;
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaUrls;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.time.Duration.ofSeconds;

@Testcontainers
@SpringBootTest
@SuppressWarnings("java:S2699")
class GreeterIT {

    private static final String DOCKER_IMAGE = "docker.redpanda.com/redpandadata/redpanda:v24.1.2";

    private static final Class<? extends LoginModule> KAFKA_LOGIN_MODULE = ScramLoginModule.class;
    private static final String                       KAFKA_USER         = "admin";
    private static final String                       KAFKA_PASSWORD     = "test";

    private static final String SASL_MECHANISM    = "SCRAM-SHA-256";
    private static final String SECURITY_PROTOCOL = "SASL_PLAINTEXT";

    private static final String INPUT_TOPIC_MEN   = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN = "input.women.avro";
    private static final String OUTPUT_TOPIC      = "output.avro";

    private static final Duration METADATA_MAX_AGE = ofSeconds(1);

    private static final String USER_SETUP = "{\"username\": \"%s\", \"password\": \"%s\", \"algorithm\": \"%s\"}";

    private static KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator                                greetingValidator;

    @Container
    private static RedpandaContainer broker = new RedpandaContainer(DOCKER_IMAGE).enableAuthorization()
                                                                                 .enableSasl()
                                                                                 .enableSchemaRegistryHttpBasicAuth()
                                                                                 .withSuperuser(KAFKA_USER);

    @Test
    void testGreetMen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.topic(INPUT_TOPIC_MEN).build();
        greetOk("Steve", greetingVisitor, greetingValidator);
    }

    @Test
    void testGreetWomen() {
        final GreetingVisitor greetingVisitor = greetingVisitorBuilder.topic(INPUT_TOPIC_WOMEN).build();
        greetOk("Laurene", greetingVisitor, greetingValidator);
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // Required properties
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("kafka.user", () -> KAFKA_USER);
        registry.add("kafka.password", () -> KAFKA_PASSWORD);
        // Overridden properties
        registry.add("spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms", METADATA_MAX_AGE::toMillis);
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
                                                                      .loginModuleClass(KAFKA_LOGIN_MODULE)
                                                                      .username(KAFKA_USER)
                                                                      .password(KAFKA_PASSWORD)
                                                                      .build();
        final KafkaGreetingFactory greetingFactory = KafkaGreetingFactory.builder().urls(urls).authentication(authentication).build();
        greetingVisitorBuilder = greetingFactory.greetingVisitorBuilder();
        greetingValidator      = greetingFactory.greetingValidatorBuilder().topic(OUTPUT_TOPIC).build();
    }

}
