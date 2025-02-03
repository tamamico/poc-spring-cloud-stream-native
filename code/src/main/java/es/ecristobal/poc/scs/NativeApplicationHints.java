package es.ecristobal.poc.scs;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.avro.Output;
import io.confluent.kafka.schemaregistry.client.rest.entities.ErrorMessage;
import io.confluent.kafka.schemaregistry.client.rest.entities.Schema;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaReference;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaString;
import io.confluent.kafka.schemaregistry.client.rest.entities.SchemaTypeConverter;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.ConfigUpdateRequest;
import io.confluent.kafka.schemaregistry.client.rest.entities.requests.RegisterSchemaRequest;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.context.NullContextNameStrategy;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.aot.hint.ExecutableMode.INVOKE;
import static org.springframework.aot.hint.MemberCategory.DECLARED_FIELDS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_CONSTRUCTORS;
import static org.springframework.aot.hint.MemberCategory.INVOKE_DECLARED_METHODS;
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
            hints.reflection()
                 .registerConstructor(accessibleConstructor(SchemaTypeConverter.class), INVOKE);
            hints.reflection()
                 .registerType(SchemaString.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS)
                 .registerConstructor(accessibleConstructor(SchemaString.class), INVOKE);
            hints.reflection()
                 .registerType(RegisterSchemaRequest.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS)
                 .registerConstructor(accessibleConstructor(RegisterSchemaRequest.class), INVOKE);
            hints.reflection()
                 .registerType(SchemaReference.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS,
                               INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection()
                 .registerType(ErrorMessage.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS,
                               INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection()
                 .registerType(ConfigUpdateRequest.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS)
                 .registerConstructor(accessibleConstructor(ConfigUpdateRequest.class), INVOKE);
            hints.reflection()
                 .registerType(Schema.class, DECLARED_FIELDS, INVOKE_DECLARED_METHODS, INVOKE_DECLARED_CONSTRUCTORS);
            hints.serialization()
                 .registerType(Input.class);
            hints.serialization()
                 .registerType(Output.class);
        } catch(NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
