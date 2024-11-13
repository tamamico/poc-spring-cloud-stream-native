package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import java.util.Map;
import java.util.UUID;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import lombok.Builder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

public class KafkaGreetingVisitor
        extends GreetingVisitor {

    private final KafkaTemplate<String, Input> template;

    @Builder
    private KafkaGreetingVisitor(final Map<String, Object> properties, final String topic) {
        final ProducerFactory<String, Input> producerFactory = new DefaultKafkaProducerFactory<>(properties);
        this.template = new KafkaTemplate<>(producerFactory, true);
        this.template.setObservationEnabled(true);
        this.template.setDefaultTopic(topic);
    }

    @Override
    public void visit(final Customer customer) {
        this.template.sendDefault(UUID.randomUUID()
                                      .toString(), this.buildFrom(customer));
    }

}
