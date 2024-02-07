package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;
import static org.testcontainers.utility.DockerImageName.parse;

@Testcontainers
@SpringBootTest
class ScsNativeApplicationTests {

    @Container
    private static KafkaContainer kafkaContainer = new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3")).withKraft();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.consumer-properties.schema.registry.url", () -> "mock://");
        registry.add("spring.cloud.stream.kafka.binder.producer-properties.schema.registry.url", () -> "mock://");
    }

    @Test
    void testSayHi() {
        final Map<String, Object> producerProperties = producerProps(kafkaContainer.getBootstrapServers());
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", "mock://");
        final DefaultKafkaProducerFactory<String, Input> pf = new DefaultKafkaProducerFactory<>(producerProperties);
        final KafkaTemplate<String, Input>               template = new KafkaTemplate<>(pf, true);
        template.setDefaultTopic("input-test");
        template.sendDefault(Input.newBuilder().setName("Steve").build());
        final Map<String, Object> consumerProperties = consumerProps(kafkaContainer.getBootstrapServers(),
                                                                     "test",
                                                                     "false"
        );
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", "mock://");
        consumerProperties.put("specific.avro.reader", true);
        final DefaultKafkaConsumerFactory<String, Output> cf = new DefaultKafkaConsumerFactory<>(consumerProperties);
        try(Consumer<String, Output> consumer = cf.createConsumer()) {
            consumer.subscribe(Collections.singleton("output-test"));
            final ConsumerRecords<String, Output> records = consumer.poll(Duration.ofSeconds(10));
            consumer.commitSync();
            assertThat(records.count()).isEqualTo(1);
            assertEquals("Hello, Steve!", records.iterator().next().value().getMessage().toString());
        }
    }

}
