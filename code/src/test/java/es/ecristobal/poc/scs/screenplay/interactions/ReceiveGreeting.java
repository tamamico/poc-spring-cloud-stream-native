package es.ecristobal.poc.scs.screenplay.interactions;

import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

public class ReceiveGreeting {

    private static final Duration POLLING_TIMEOUT = ofSeconds(10);

    private final Consumer<String, Output> consumer;

    public ReceiveGreeting(
            final String brokerUrl,
            final String schemaRegistryUrl,
            final String topic
    ) {
        final Map<String, Object> consumerProperties = consumerProps(brokerUrl, "test", "false");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", schemaRegistryUrl);
        consumerProperties.put("specific.avro.reader", true);
        final ConsumerFactory<String, Output> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton(topic));
    }

    public void check(final java.util.function.Consumer<String> assertions) {
        final ConsumerRecords<String, Output> records = consumer.poll(POLLING_TIMEOUT);
        consumer.commitSync();
        assertEquals(1, records.count());
        final String message = records.iterator().next().value().getMessage().toString();
        assertions.accept(message);
    }
}
