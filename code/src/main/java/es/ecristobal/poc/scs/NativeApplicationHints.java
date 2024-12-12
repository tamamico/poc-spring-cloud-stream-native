package es.ecristobal.poc.scs;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.context.NullContextNameStrategy;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;
import static org.springframework.util.ReflectionUtils.accessibleConstructor;

class NativeApplicationHints
        implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        try {
            hints.reflection()
                 .registerConstructor(accessibleConstructor(KafkaAvroSerializer.class), INVOKE);
            hints.reflection()
                 .registerConstructor(accessibleConstructor(KafkaAvroDeserializer.class), INVOKE);
            hints.reflection()
                 .registerConstructor(accessibleConstructor(NullContextNameStrategy.class), INVOKE);
            hints.reflection()
                 .registerConstructor(accessibleConstructor(RecordNameStrategy.class), INVOKE);
            hints.reflection()
                 .registerConstructor(accessibleConstructor(TopicNameStrategy.class), INVOKE);
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
