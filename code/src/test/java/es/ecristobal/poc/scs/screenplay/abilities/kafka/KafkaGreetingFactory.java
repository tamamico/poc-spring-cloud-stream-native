package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingFactory;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

public class KafkaGreetingFactory
        implements GreetingFactory {

    private static final String JAAS_CONFIG_TEMPLATE
            = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";

    private final Map<String, Object> consumerProperties;
    private final Map<String, Object> producerProperties;

    private KafkaGreetingFactory() {
        this.producerProperties = new HashMap<>();
        this.producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        this.consumerProperties = new HashMap<>();
        this.consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumerProperties.put("specific.avro.reader", true);
        this.consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
    }

    public static KafkaGreetingFactory newInstance() {
        return new KafkaGreetingFactory();
    }

    public KafkaGreetingFactory withUrls(
            final String broker,
            final String schemaRegistry
    ) {
        consumerProps(broker, "greeting-validator", "true").forEach(this.consumerProperties::putIfAbsent);
        this.consumerProperties.put("schema.registry.url", schemaRegistry);
        producerProps(broker).forEach(this.producerProperties::putIfAbsent);
        this.producerProperties.put("schema.registry.url", schemaRegistry);
        return this;
    }

    public KafkaGreetingFactory withAuthentication(
            final String username,
            final String password
    ) {
        final Map<String, Object> authentication = new HashMap<>(3);
        authentication.put("security.protocol", "SASL_PLAINTEXT");
        authentication.put("sasl.mechanism", "SCRAM-SHA-256");
        authentication.put("sasl.jaas.config", format(JAAS_CONFIG_TEMPLATE, username, password));
        this.consumerProperties.putAll(authentication);
        this.producerProperties.putAll(authentication);
        return this;
    }

    @Override
    public KafkaGreetingVisitorBuilder greetingVisitorBuilder() {
        return KafkaGreetingVisitorBuilder.newInstance().withProperties(this.producerProperties);
    }

    @Override
    public KafkaGreetingValidatorBuilder greetingValidatorBuilder() {
        return KafkaGreetingValidatorBuilder.newInstance().withProperties(this.consumerProperties);
    }
}
