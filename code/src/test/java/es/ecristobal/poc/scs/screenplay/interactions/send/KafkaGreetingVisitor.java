package es.ecristobal.poc.scs.screenplay.interactions.send;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

class KafkaGreetingVisitor
        extends GreetingVisitor {

    private final KafkaTemplate<String, Input> template;

    KafkaGreetingVisitor(
            final Map<String, Object> properties,
            final String topic
    ) {
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(properties);
        this.template = new KafkaTemplate<>(producerFactory, true);
        this.template.setDefaultTopic(topic);
    }

    @Override
    public void visit(final Customer customer) {
        this.template.sendDefault(this.buildFrom(customer));
    }
}
