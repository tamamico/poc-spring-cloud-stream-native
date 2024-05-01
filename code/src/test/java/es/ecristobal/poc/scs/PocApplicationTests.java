package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.util.Collections;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

@SpringBootTest
@Testcontainers
class PocApplicationTests {

    private static final String INPUT_TOPIC_NAME  = "input-topic";
    private static final String OUTPUT_TOPIC_NAME = "output-topic";

    @Container
    private static RedpandaContainer broker = new RedpandaContainer("docker.redpanda.com/redpandadata/redpanda:v23.1.2");

    private static KafkaTemplate<String, Input> template;
    private static Consumer<String, Output>     consumer;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", broker::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.configuration.schema.registry.url", broker::getSchemaRegistryAddress);
        registry.add("spring.cloud.stream.bindings.greeter-in-0.destination", () -> INPUT_TOPIC_NAME);
        registry.add("spring.cloud.stream.bindings.greeter-out-0.destination", () -> OUTPUT_TOPIC_NAME);
    }

    @BeforeAll
    static void setUp() {
        // Set-up KafkaTemplate
        final Map<String, Object> producerProperties = producerProps(broker.getBootstrapServers());
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", broker.getSchemaRegistryAddress());
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        template = new KafkaTemplate<>(producerFactory, true);
        // Set-up ConsumerFactory
        final Map<String, Object> consumerProperties = consumerProps(broker.getBootstrapServers(), "test", "false");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", broker.getSchemaRegistryAddress());
        consumerProperties.put("specific.avro.reader", true);
        final ConsumerFactory<String, Output> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton(OUTPUT_TOPIC_NAME));
    }

    @Test
    void testSayHi() {
        // Send message
        template.send(INPUT_TOPIC_NAME, Input.newBuilder().setName("Steve").build());
        // Consume messages from topic
        final ConsumerRecords<String, Output> records = consumer.poll(ofSeconds(10));
        consumer.commitSync();
        assertThat(records.count()).isEqualTo(1);
        assertEquals("Hello, Steve!", records.iterator().next().value().getMessage().toString());
    }

}
