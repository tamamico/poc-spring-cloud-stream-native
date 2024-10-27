package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static es.ecristobal.poc.scs.TestScenarios.greetOk;
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaAuthentication;
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaUrls;

@AutoConfigureObservability
@SpringBootTest
@SuppressWarnings({"java:S2699", "java:S3577"})
class GreeterItConfluent {

    private static final String KAFKA_BROKER_URL    = "TBD";
    private static final String SCHEMA_REGISTRY_URL = "TBD";

    private static final String KAFKA_TEST_USER               = "TBD";
    private static final String KAFKA_TEST_PASSWORD           = "TBD";
    private static final String SCHEMA_REGISTRY_TEST_USER     = "TBD";
    private static final String SCHEMA_REGISTRY_TEST_PASSWORD = "TBD";

    private static final String INPUT_TOPIC_MEN   = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN = "input.women.avro";
    private static final String OUTPUT_TOPIC      = "output.avro";

    private static KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator                                greetingValidator;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", () -> KAFKA_BROKER_URL);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", () -> SCHEMA_REGISTRY_URL);
        registry.add("kafka.user", () -> "TBD");
        registry.add("kafka.password", () -> "TBD");
        registry.add("schema-registry.user", () -> "TBD");
        registry.add("schema-registry.password", () -> "TBD");
    }

    @BeforeAll
    static void setUp() {
        final KafkaUrls urls = KafkaUrls.builder().broker(KAFKA_BROKER_URL).schemaRegistry(SCHEMA_REGISTRY_URL).build();
        final KafkaAuthentication authentication = KafkaAuthentication.builder()
                                                                      .type(KafkaAuthentication.AuthenticationType.PLAIN)
                                                                      .kafkaUsername(KAFKA_TEST_USER)
                                                                      .kafkaPassword(KAFKA_TEST_PASSWORD)
                                                                      .schemaRegistryUsername(SCHEMA_REGISTRY_TEST_USER)
                                                                      .schemaRegistryPassword(SCHEMA_REGISTRY_TEST_PASSWORD)
                                                                      .build();
        final KafkaGreetingFactory greetingFactory = KafkaGreetingFactory.builder()
                                                                         .urls(urls)
                                                                         .authentication(authentication)
                                                                         .autoRegisterSchemas(false)
                                                                         .build();
        greetingVisitorBuilder = greetingFactory.greetingVisitorBuilder();
        greetingValidator      = greetingFactory.greetingValidatorBuilder().topic(OUTPUT_TOPIC).build();
    }

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

}
