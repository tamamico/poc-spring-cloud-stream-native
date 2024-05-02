package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.interactions.receive.KafkaBinderGreetingValidator;
import es.ecristobal.poc.scs.screenplay.interactions.send.KafkaBinderGreetingVisitor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

@SpringBootTest
@Testcontainers
class SystemTests
        extends ApplicationBaseTests {

    private static final String INPUT_TOPIC_NAME  = "input-topic";
    private static final String OUTPUT_TOPIC_NAME = "output-topic";

    @Container
    private static RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.bindings.greeter-in-0.destination", () -> INPUT_TOPIC_NAME);
        registry.add("spring.cloud.stream.bindings.greeter-out-0.destination", () -> OUTPUT_TOPIC_NAME);
    }

    @BeforeAll
    static void setUp() {
        greetingVisitor   = new KafkaBinderGreetingVisitor(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(),
                                                           INPUT_TOPIC_NAME);
        greetingValidator = new KafkaBinderGreetingValidator(broker.getBootstrapServers(), broker.getSchemaRegistryAddress(),
                                                             OUTPUT_TOPIC_NAME);
    }

}
