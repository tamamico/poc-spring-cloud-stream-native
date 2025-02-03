package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaAuthentication.AuthenticationType;
import es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaAuthentication;
import static es.ecristobal.poc.scs.screenplay.abilities.kafka.KafkaGreetingFactory.KafkaUrls;

@SuppressWarnings({"java:S2699", "java:S3577"})
class MessageProducer
        extends TestScenarios {

    private static final String KAFKA_BROKER        = System.getenv("KAFKA_BROKER");
    private static final String SCHEMA_REGISTRY_URL = System.getenv("SCHEMA_REGISTRY_URL");

    private static final String KAFKA_USER     = System.getenv("KAFKA_USER");
    private static final String KAFKA_PASSWORD = System.getenv("KAFKA_PASSWORD");

    private static final String SCHEMA_REGISTRY_USER     = System.getenv("SCHEMA_REGISTRY_USER");
    private static final String SCHEMA_REGISTRY_PASSWORD = System.getenv("SCHEMA_REGISTRY_PASSWORD");

    private static final String INPUT_TOPIC_MEN   = "input.men.avro";
    private static final String INPUT_TOPIC_WOMEN = "input.women.avro";
    private static final String OUTPUT_TOPIC      = "output.avro";

    private static KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder;
    private static GreetingValidator                                greetingValidator;

    @BeforeAll
    static void setUp() {
        final KafkaUrls urls = KafkaUrls.builder()
                                        .broker(KAFKA_BROKER)
                                        .schemaRegistry(SCHEMA_REGISTRY_URL)
                                        .build();
        final KafkaAuthentication authentication = KafkaAuthentication.builder()
                                                                      .type(AuthenticationType.PLAIN)
                                                                      .kafkaUsername(KAFKA_USER)
                                                                      .kafkaPassword(KAFKA_PASSWORD)
                                                                      .schemaRegistryUsername(SCHEMA_REGISTRY_USER)
                                                                      .schemaRegistryPassword(SCHEMA_REGISTRY_PASSWORD)
                                                                      .build();
        final KafkaGreetingFactory greetingFactory = KafkaGreetingFactory.builder()
                                                                         .urls(urls)
                                                                         .authentication(authentication)
                                                                         .autoRegisterSchemas(false)
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
