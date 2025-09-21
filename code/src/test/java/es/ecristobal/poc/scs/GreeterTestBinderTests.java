package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.testbinder.TestBinderGreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.testbinder.TestBinderGreetingVisitor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.stream.binder.test.EnableTestBinder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.schema.registry.avro.AvroSchemaMessageConverter;
import org.springframework.cloud.stream.schema.registry.avro.AvroSchemaServiceManagerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.MimeType;

@SpringBootTest
@EnableTestBinder
@SuppressWarnings("java:S2699")
class GreeterTestBinderTests
        extends TestScenarios {

    private static final MimeType AVRO_MIME_TYPE = MimeType.valueOf("avro/bytes");

    @TestConfiguration
    static class TestBinderConfiguration {

        @Bean
        MessageConverter avroMessageConverter() {
            return new AvroSchemaMessageConverter(AVRO_MIME_TYPE, new AvroSchemaServiceManagerImpl());
        }
    }

    private final GreetingVisitor   greetingVisitor;
    private final GreetingValidator greetingValidator;

    @Autowired
    GreeterTestBinderTests(final InputDestination input, final OutputDestination output) {
        this.greetingVisitor   = TestBinderGreetingVisitor.builder()
                                                          .inputDestination(input)
                                                          .build();
        this.greetingValidator = TestBinderGreetingValidator.builder()
                                                            .outputDestination(output)
                                                            .build();
    }

    @DynamicPropertySource
    static void setUp(final DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.bindings.greet-out-0.producer.use-native-encoding", () -> false);
        registry.add("spring.cloud.stream.bindings.greet-out-0.content-type", () -> AVRO_MIME_TYPE);
    }

    @Test
    void testGreetMen() {
        this.greetOk("Steve", this.greetingVisitor, this.greetingValidator);
    }

    @Test
    void testGreetWomen() {
        this.greetOk("Laurene", this.greetingVisitor, this.greetingValidator);
    }

}
