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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;
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

class ScsNativeApplicationIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScsNativeApplicationIT.class);

    private static final DockerImageName KAFKA_IMAGE           = parse("confluentinc/cp-kafka:latest");
    private static final DockerImageName SCHEMA_REGISTRY_IMAGE = parse("confluentinc/cp-schema-registry:latest");
    private static final DockerImageName APP_IMAGE             = parse("poc-scs-native:0.1.0-SNAPSHOT");

    private static final int SCHEMA_REGISTRY_PORT = 8081;

    private static final Network             network  = Network.newNetwork();
    private static final KafkaContainer      kafka    = new KafkaContainer(KAFKA_IMAGE)
            .withNetwork(network)
            .withKraft();
    private static final GenericContainer<?> registry = new GenericContainer<>(SCHEMA_REGISTRY_IMAGE)
            .withNetwork(network)
            .withExposedPorts(SCHEMA_REGISTRY_PORT);
    private static final GenericContainer<?> app      = new GenericContainer<>(APP_IMAGE)
            .withNetwork(network);

    private static String schemaRegistryUrl;

    @BeforeAll
    public static void setUp() {
        kafka.start();
        final String bootstrapServers = format("%s:9092", kafka.getContainerName());
        registry.withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS", bootstrapServers)
                .withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema_registry")
                .start();
        schemaRegistryUrl = format("http://%s:%d", registry.getHost(), registry.getMappedPort(SCHEMA_REGISTRY_PORT));
        final String dockerSchemaRegistryUrl = format("http:/%s:%d", registry.getContainerName(), SCHEMA_REGISTRY_PORT);
        app.withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS", bootstrapServers)
           .withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_CONSUMERPROPERTIES_SCHEMA_REGISTRY_URL", dockerSchemaRegistryUrl)
           .withEnv("SPRING_CLOUD_STREAM_KAFKA_BINDER_PRODUCERPROPERTIES_SCHEMA_REGISTRY_URL", dockerSchemaRegistryUrl)
           .start();
        final Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams();
        app.followOutput(logConsumer);
    }

    @AfterAll
    public static void tearDown() {
        app.stop();
        registry.stop();
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
        template.setDefaultTopic("input-test");
        final Map<String, Object> consumerProperties = consumerProps(kafka.getBootstrapServers(), "test", "false");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", schemaRegistryUrl);
        consumerProperties.put("specific.avro.reader", true);
        final DefaultKafkaConsumerFactory<String, Output> cf = new DefaultKafkaConsumerFactory<>(consumerProperties);
        template.sendDefault(Input.newBuilder().setName("Steve").build());
        try(Consumer<String, Output> consumer = cf.createConsumer()) {
            consumer.subscribe(Collections.singleton("output-test"));
            final ConsumerRecords<String, Output> records = consumer.poll(Duration.ofSeconds(10));
            consumer.commitSync();
            assertThat(records.count()).isEqualTo(1);
            assertEquals("Hello, Steve!", records.iterator().next().value().getMessage().toString());
        }
    }

}
