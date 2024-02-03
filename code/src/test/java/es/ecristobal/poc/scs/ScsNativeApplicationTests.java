package es.ecristobal.poc.scs;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.utility.DockerImageName.parse;

@Testcontainers
@SpringBootTest
class ScsNativeApplicationTests {

    @Container
    private static KafkaContainer container = new KafkaContainer(parse("confluentinc/cp-kafka:7.5.3")).withKraft();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", () -> container.getBootstrapServers());
    }

    @Test
    void testSayHi() {
        final Map<String, Object> senderProps = KafkaTestUtils.producerProps(container.getBootstrapServers());
        senderProps.put("key.serializer", StringSerializer.class);
        senderProps.put("value.serializer", StringSerializer.class);

        final DefaultKafkaProducerFactory<String, String> pf       = new DefaultKafkaProducerFactory<>(senderProps);
        final KafkaTemplate<String, String>               template = new KafkaTemplate<>(pf, true);
        template.setDefaultTopic("input-test");
        template.sendDefault("Steve");

        final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(container.getBootstrapServers(),
                                                                               "test",
                                                                               "false"
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put("key.deserializer", StringDeserializer.class);
        consumerProps.put("value.deserializer", StringDeserializer.class);

        final DefaultKafkaConsumerFactory<String, String> cf       = new DefaultKafkaConsumerFactory<>(consumerProps);
        final Consumer<String, String>                    consumer = cf.createConsumer();
        consumer.subscribe(Collections.singleton("output-test"));

        final ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        consumer.commitSync();

        assertThat(records.count()).isEqualTo(1);
        assertThat(records.iterator().next().value()).isEqualTo("Hello, Steve");
    }

}
