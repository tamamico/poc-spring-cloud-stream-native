package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaGreetingValidator
        implements GreetingValidator {

    private static final Duration POLLING_TIMEOUT = ofSeconds(10);

    private final Consumer<String, Output> consumer;
    private final List<TopicPartition>     topicPartitions;

    @Builder
    KafkaGreetingValidator(
            final Map<String, Object> properties,
            final String topic
    ) {
        final ConsumerFactory<String, Output> consumerFactory = new DefaultKafkaConsumerFactory<>(properties);
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
