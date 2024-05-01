package es.ecristobal.poc.scs.screenplay.interactions;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

public class SendGreeting {

    private final KafkaTemplate<String, Input> template;

    public SendGreeting(
            final String brokerUrl,
            final String schemaRegistryUrl,
            final String topic
    ) {
        final Map<String, Object> producerProperties = producerProps(brokerUrl);
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        producerProperties.put("schema.registry.url", schemaRegistryUrl);
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
        this.template = new KafkaTemplate<>(producerFactory, true);
        this.template.setDefaultTopic(topic);
    }

    public void to(final Customer customer) {
        this.template.sendDefault(this.buildFrom(customer));
    }

    private Input buildFrom(final Customer customer) {
        return Input.newBuilder().setName(customer.name()).build();
    }
}
