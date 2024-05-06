package es.ecristobal.poc.scs.screenplay.interactions.receive;

import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

public class KafkaBinderGreetingValidator
        implements GreetingValidator {

    private static final Duration POLLING_TIMEOUT = Duration.ofSeconds(10);

    private final Consumer<String, Output> consumer;
    private final List<TopicPartition>     topicPartitions;

    public KafkaBinderGreetingValidator(
            final String brokerUrl,
            final String schemaRegistryUrl,
            final String topic
    ) {
        final Map<String, Object> consumerProperties = consumerProps(brokerUrl, "greeting-validator", "true");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProperties.put("schema.registry.url", schemaRegistryUrl);
        consumerProperties.put("specific.avro.reader", true);
        final ConsumerFactory<String, Output> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties);
        consumer = consumerFactory.createConsumer();
        final TopicPartition topicPartition = new TopicPartition(topic, 0);
        topicPartitions = List.of(topicPartition);
        consumer.assign(topicPartitions);
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        consumer.seekToEnd(this.topicPartitions);
        final ConsumerRecords<String, Output> records = consumer.poll(POLLING_TIMEOUT);
        assertEquals(1, records.count());
        final String message = records.iterator().next().value().getMessage().toString();
        assertions.accept(message);
    }
}
