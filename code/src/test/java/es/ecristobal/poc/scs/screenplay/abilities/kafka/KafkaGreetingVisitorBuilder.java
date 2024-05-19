package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitorBuilder;

import java.util.Map;

public class KafkaGreetingVisitorBuilder
        implements GreetingVisitorBuilder {

    private Map<String, Object> properties;

    private String topic;

    private KafkaGreetingVisitorBuilder() {
        // Empty constructor
    }

    public static KafkaGreetingVisitorBuilder newInstance() {
        return new KafkaGreetingVisitorBuilder();
    }

    public KafkaGreetingVisitorBuilder withProperties(final Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public KafkaGreetingVisitorBuilder withTopic(final String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public GreetingVisitor build() {
        return new KafkaGreetingVisitor(this.properties, this.topic);
    }
}
