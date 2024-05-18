package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;

public class KafkaGreetingValidatorBuilder {

    private static final String JAAS_CONFIG_TEMPLATE
            = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";

    private final Map<String, Object> properties;

    private KafkaGreetingValidatorBuilder() {
        this.properties = new HashMap<>();
        this.properties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        this.properties.put("specific.avro.reader", true);
    }

    public static KafkaGreetingValidatorBuilder newInstance() {
        return new KafkaGreetingValidatorBuilder();
    }

    public KafkaGreetingValidatorBuilder withUrls(
            final String broker,
            final String schemaRegistry
    ) {
        consumerProps(broker, "greeting-validator", "true").forEach(this.properties::putIfAbsent);
        this.properties.put("schema.registry.url", schemaRegistry);
        return this;
    }

    public KafkaGreetingValidatorBuilder withAuthentication(
            final String username,
            final String password
    ) {
        this.properties.put("security.protocol", "SASL_PLAINTEXT");
        this.properties.put("sasl.mechanism", "SCRAM-SHA-256");
        this.properties.put("sasl.jaas.config", format(JAAS_CONFIG_TEMPLATE, username, password));
        return this;
    }

    public GreetingValidator build(final String topic) {
        return new KafkaGreetingValidator(this.properties, topic);
    }
}
