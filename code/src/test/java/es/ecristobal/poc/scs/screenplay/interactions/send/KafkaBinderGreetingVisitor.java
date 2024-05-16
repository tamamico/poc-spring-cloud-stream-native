package es.ecristobal.poc.scs.screenplay.interactions.send;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

public class KafkaBinderGreetingVisitor
        implements GreetingVisitor {

    private final KafkaTemplate<String, Input> template;

    public KafkaBinderGreetingVisitor(
            final String brokerUrl,
            final String schemaRegistryUrl,
            final String topic,
            final String username,
            final String password
    ) {
        final Map<String, Object> producerProperties = producerProps(brokerUrl);
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", schemaRegistryUrl);
        producerProperties.put("security.protocol", "SASL_PLAINTEXT");
        producerProperties.put("sasl.mechanism", "SCRAM-SHA-256");
        producerProperties.put("sasl.jaas.config", format("org.apache.kafka.common.security.scram.ScramLoginModule required " +
                                                          "username=\"%s\" password=\"%s\";", username, password));
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        this.template = new KafkaTemplate<>(producerFactory, true);
        this.template.setDefaultTopic(topic);
    }

    @Override
    public void visit(final Customer customer) {
        this.template.sendDefault(this.buildFrom(customer));
    }

    private Input buildFrom(final Customer customer) {
        return Input.newBuilder().setName(customer.name()).build();
    }
}
