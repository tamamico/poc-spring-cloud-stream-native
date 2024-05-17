package es.ecristobal.poc.scs.screenplay.interactions.send;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

public class GreetingVisitorBuilder {

    private GreetingVisitorBuilder() {
    }

    public static KafkaGreetingVisitorBuilder withKafka() {
        return new KafkaGreetingVisitorBuilder();
    }

    public static class KafkaGreetingVisitorBuilder {

        private static final String JAAS_CONFIG_TEMPLATE
                = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";

        private final Map<String, Object> properties;

        private KafkaGreetingVisitorBuilder() {
            this.properties = new HashMap<>();
            this.properties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            this.properties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        }

        public KafkaGreetingVisitorBuilder withUrls(
                final String broker,
                final String schemaRegistry
        ) {
            producerProps(broker).forEach(this.properties::putIfAbsent);
            this.properties.put("schema.registry.url", schemaRegistry);
            return this;
        }

        public KafkaGreetingVisitorBuilder withAuthentication(
                final String username,
                final String password
        ) {
            this.properties.put("security.protocol", "SASL_PLAINTEXT");
            this.properties.put("sasl.mechanism", "SCRAM-SHA-256");
            this.properties.put("sasl.jaas.config", format(JAAS_CONFIG_TEMPLATE, username, password));
            return this;
        }

        public GreetingVisitor build(final String topic) {
            return new KafkaGreetingVisitor(this.properties, topic);
        }
    }
}
