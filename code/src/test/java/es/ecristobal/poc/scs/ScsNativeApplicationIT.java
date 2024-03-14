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
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
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
class ScsNativeApplicationIT {

    private static final String SCHEMA_REGISTRY_URL = "mock://";

    @Container
    private static KafkaContainer kafka =
            new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3")).withKraft();

    @Container
    private static GenericContainer<?> app = new GenericContainer<>(parse("poc-scs-native:0.1.0-SNAPSHOT"))
            .dependsOn(kafka);

    @BeforeAll
    public static void setUp() {
        kafka.start();
        app.withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS", kafka.getBootstrapServers())
           .withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_CONSUMERPROPERTIES_SCHEMA_REGISTRY_URL", SCHEMA_REGISTRY_URL)
           .withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_PRODUCERPROPERTIES_SCHEMA_REGISTRY_URL", SCHEMA_REGISTRY_URL)
           .start();
    }

    @Test
    void testSayHi() {
        final Map<String, Object> producerProperties = producerProps(kafka.getBootstrapServers());
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        final KafkaTemplate<String, Input>   template        = new KafkaTemplate<>(producerFactory, true);
        template.setDefaultTopic("input-test");
        template.sendDefault(Input.newBuilder().setName("Steve").build());
        final Map<String, Object> consumerProperties = consumerProps(kafka.getBootstrapServers(), "test", "false");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", SCHEMA_REGISTRY_URL);
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
