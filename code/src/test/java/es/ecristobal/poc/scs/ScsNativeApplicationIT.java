package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
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
import static org.testcontainers.utility.DockerImageName.parse;

@SpringBootTest
class ScsNativeApplicationIT {

    private static final DockerImageName KAFKA_IMAGE           = parse("confluentinc/cp-kafka:7.6.1");
    private static final DockerImageName SCHEMA_REGISTRY_IMAGE = parse("confluentinc/cp-schema-registry:7.6.1");

    private static final int SCHEMA_REGISTRY_PORT = 8081;

    private static final Network network = Network.SHARED;

    private static final KafkaContainer      kafka          = new KafkaContainer(KAFKA_IMAGE)
            .withNetwork(network)
            .withKraft();
    private static final GenericContainer<?> schemaRegistry = new GenericContainer<>(SCHEMA_REGISTRY_IMAGE)
            .withNetwork(network)
            .withExposedPorts(SCHEMA_REGISTRY_PORT);

    private static String schemaRegistryUrl;

    @BeforeAll
    public static void setUp() {
        kafka.start();
        schemaRegistry.withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
                               format("%s:9092", kafka.getContainerName().substring(1))
        ).withEnv("SCHEMA_REGISTRY_HOST_NAME", "localhost").start();
        schemaRegistryUrl = format("http://%s:%d",
                                   schemaRegistry.getHost(),
                                   schemaRegistry.getMappedPort(SCHEMA_REGISTRY_PORT)
        );
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
        registry.add("spring.cloud.stream.kafka.binder.consumer-properties.schema.registry.url",
                     () -> schemaRegistryUrl
        );
        registry.add("spring.cloud.stream.kafka.binder.producer-properties.schema.registry.url",
                     () -> schemaRegistryUrl
        );
    }

    @AfterAll
    public static void tearDown() {
        schemaRegistry.stop();
        kafka.stop();
    }

    @Test
    void testSayHi() {
        final Map<String, Object> producerProperties = producerProps(kafka.getBootstrapServers());
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", schemaRegistryUrl);
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        final KafkaTemplate<String, Input>   template        = new KafkaTemplate<>(producerFactory, true);
        template.send("input-test", Input.newBuilder().setName("Steve").build());
        final Map<String, Object> consumerProperties = consumerProps(kafka.getBootstrapServers(), "test", "false");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", schemaRegistryUrl);
        consumerProperties.put("specific.avro.reader", true);
        final DefaultKafkaConsumerFactory<String, Output> cf = new DefaultKafkaConsumerFactory<>(consumerProperties);
        try(Consumer<String, Output> consumer = cf.createConsumer()) {
            consumer.subscribe(Collections.singleton("output-test"));
            final ConsumerRecords<String, Output> records = consumer.poll(ofSeconds(10));
            consumer.commitSync();
            assertThat(records.count()).isEqualTo(1);
            assertEquals("Hello, Steve!", records.iterator().next().value().getMessage().toString());
        }
    }

}
