package es.ecristobal.poc.scs;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.redpanda.RedpandaContainer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class ScsNativeApplicationTests {

    @Container
    private static RedpandaContainer container = new RedpandaContainer(
            "docker.redpanda.com/redpandadata/redpanda:v23.3.4");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", () -> container.getBootstrapServers());
    }

    @Test
    void testSayHi() {
        final String              name        = "Steve";
        final Map<String, Object> senderProps = KafkaTestUtils.producerProps(container.getBootstrapServers());
        senderProps.put("key.serializer", StringSerializer.class);
        senderProps.put("value.serializer", StringSerializer.class);

        final DefaultKafkaProducerFactory<String, String> pf       = new DefaultKafkaProducerFactory<>(senderProps);
        final KafkaTemplate<String, String>               template = new KafkaTemplate<>(pf, true);
        template.setDefaultTopic("input-test");
        template.sendDefault(name);

        final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(container.getBootstrapServers(),
                                                                               "test",
                                                                               "false"
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);

        final DefaultKafkaConsumerFactory<String, String> cf       = new DefaultKafkaConsumerFactory<>(consumerProps);
        final Consumer<String, String>                    consumer = cf.createConsumer();
        consumer.assign(Collections.singleton(new TopicPartition("output-test", 0)));

        final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        consumer.commitSync();

        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo("Hello, Steve");
    }

}
