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

    private static final String KAFKA_BROKER_URL    = "SASL_SSL://pkc-p11xm.us-east-1.aws.confluent.cloud:9092";
    private static final String SCHEMA_REGISTRY_URL = "https://psrc-6kq702.us-east-1.aws.confluent.cloud";

    private static final String KAFKA_TEST_USER               = "OSCWAJOIHI4TP4TP";
    private static final String KAFKA_TEST_PASSWORD           = "VMZWwjWrpQkl1tlGmg35xvQErGtekmdx6GOPZxT7DcfbeLvqgYyd64xu6rw/HLzb";
    private static final String SCHEMA_REGISTRY_TEST_USER     = "KG46V6CIPLYYRHN5";
    private static final String SCHEMA_REGISTRY_TEST_PASSWORD = "oed2c291GtsZ97b+p+GULH2EilTD6CfEpUioSNTuq7XZh+NuLSBzU5sk8nkK4rX7";

    private static final String INPUT_TOPIC_MEN   = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN = "input.women.avro";
    private static final String OUTPUT_TOPIC      = "output.avro";

    private static KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator                                greetingValidator;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", () -> KAFKA_BROKER_URL);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", () -> SCHEMA_REGISTRY_URL);
        registry.add("kafka.user", () -> "YJWEKSEFWZHP7ACS");
        registry.add("kafka.password", () -> "FaeUwVE2KcTX384VZ95k+LTKoiH9McgsSH//Y9eDCALksW34pxyfpTyt3PoNONsZ");
        registry.add("schema-registry.user", () -> "MNXW6AGHW5L2E4WD");
        registry.add("schema-registry.password", () -> "Ob0TwMwBiMj0nWvxxSz6MQ/59pDKKsBMd6hL1OdpZ7tw6g8tM0cbR1NsqGldc2rI");
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
