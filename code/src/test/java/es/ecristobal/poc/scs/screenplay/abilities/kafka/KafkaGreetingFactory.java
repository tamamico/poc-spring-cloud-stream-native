package es.ecristobal.poc.scs.screenplay.abilities.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.spi.LoginModule;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import static java.lang.String.format;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG;
import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.VALUE_SUBJECT_NAME_STRATEGY;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.test.utils.KafkaTestUtils.consumerProps;
import static org.springframework.kafka.test.utils.KafkaTestUtils.producerProps;

public class KafkaGreetingFactory {

    private static final String JAAS_CONFIG_TEMPLATE = "%s required username=\"%s\" password=\"%s\";";
    private static final String BASIC_AUTH_TEMPLATE  = "%s:%s";

    private final Map<String, Object> consumerProperties;
    private final Map<String, Object> producerProperties;

    @Builder
    private KafkaGreetingFactory(
            final KafkaUrls urls,
            final KafkaAuthentication authentication,
            final boolean autoRegisterSchemas
    ) {
        this();
        consumerProps(urls.broker(), "greeting-validator", "true").forEach(this.consumerProperties::putIfAbsent);
        this.consumerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, urls.schemaRegistry());
        producerProps(urls.broker()).forEach(this.producerProperties::putIfAbsent);
        this.producerProperties.put(SCHEMA_REGISTRY_URL_CONFIG, urls.schemaRegistry());
        this.producerProperties.put(AUTO_REGISTER_SCHEMAS, autoRegisterSchemas);
        final Map<String, Object> authenticationMap = this.getAuthenticationMap(authentication);
        this.consumerProperties.putAll(authenticationMap);
        this.producerProperties.putAll(authenticationMap);
    }

    private KafkaGreetingFactory() {
        this.producerProperties = new HashMap<>();
        this.producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        this.producerProperties.put(VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class);
        this.consumerProperties = new HashMap<>();
        this.consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumerProperties.put("specific.avro.reader", true);
        this.consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        this.consumerProperties.put(VALUE_SUBJECT_NAME_STRATEGY, RecordNameStrategy.class);
    }

    private Map<String, Object> getAuthenticationMap(final KafkaAuthentication authentication) {
        final Map<String, Object> authenticationMap = new HashMap<>(5);
        authenticationMap.put("security.protocol", authentication.type()
                                                                 .securityProtocol());
        authenticationMap.put("sasl.mechanism", authentication.type()
                                                              .saslMechanism());
        authenticationMap.put("sasl.jaas.config", format(JAAS_CONFIG_TEMPLATE, authentication.type()
                                                                                             .loginModuleClassName(),
                                                         authentication.kafkaUsername(),
                                                         authentication.kafkaPassword()));
        authenticationMap.put(BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
        authenticationMap.put(USER_INFO_CONFIG, format(BASIC_AUTH_TEMPLATE, authentication.schemaRegistryUsername(),
                                                       authentication.schemaRegistryPassword()));
        return authenticationMap;
    }

    public KafkaGreetingVisitor.KafkaGreetingVisitorBuilder greetingVisitorBuilder() {
        return KafkaGreetingVisitor.builder()
                                   .properties(this.producerProperties);
    }

    public KafkaGreetingValidator.KafkaGreetingValidatorBuilder greetingValidatorBuilder() {
        return KafkaGreetingValidator.builder()
                                     .properties(this.consumerProperties);
    }

    @Builder
    public record KafkaUrls(
            String broker, String schemaRegistry
    ) {}

    @Builder
    public record KafkaAuthentication(
            AuthenticationType type,
            String kafkaUsername,
            String kafkaPassword,
            String schemaRegistryUsername,
            String schemaRegistryPassword
    ) {

        @Getter
        public enum AuthenticationType {
            PLAIN("PLAIN", "SASL_SSL", PlainLoginModule.class),
            SCRAM("SCRAM-SHA-256", "SASL_PLAINTEXT", ScramLoginModule.class);

            private final String saslMechanism;
            private final String securityProtocol;
            private final String loginModuleClassName;

            AuthenticationType(
                    final String saslMechanism,
                    final String securityProtocol,
                    final Class<? extends LoginModule> loginModule
            ) {
                this.saslMechanism        = saslMechanism;
                this.securityProtocol     = securityProtocol;
                this.loginModuleClassName = loginModule.getName();
            }
        }

    }

}
