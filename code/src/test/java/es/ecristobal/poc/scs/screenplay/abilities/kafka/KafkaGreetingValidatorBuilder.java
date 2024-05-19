package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidatorBuilder;

import java.util.Map;

public class KafkaGreetingValidatorBuilder
        implements GreetingValidatorBuilder {

    private Map<String, Object> properties;

    private String topic;

    private KafkaGreetingValidatorBuilder() {
        // Empty constructor
    }

    public static KafkaGreetingValidatorBuilder newInstance() {
        return new KafkaGreetingValidatorBuilder();
    }

    public KafkaGreetingValidatorBuilder withProperties(final Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public KafkaGreetingValidatorBuilder withTopic(final String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    public GreetingValidator build() {
        return new KafkaGreetingValidator(this.properties, this.topic);
    }
}
