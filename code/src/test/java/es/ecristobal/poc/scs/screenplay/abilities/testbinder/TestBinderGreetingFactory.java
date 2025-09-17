package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import javax.security.auth.spi.LoginModule;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.security.scram.ScramLoginModule;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;

public class TestBinderGreetingFactory {

    private final InputDestination  inputDestination;
    private final OutputDestination outputDestination;

    @Builder
    private TestBinderGreetingFactory(
            final InputDestination inputDestination,
            final OutputDestination outputDestination
    ) {
        this.inputDestination  = inputDestination;
        this.outputDestination = outputDestination;
    }

    public GreetingVisitor greetingVisitor() {
        return new TestBinderGreetingVisitor(this.inputDestination);
    }

    public GreetingValidator greetingValidator() {
        return new TestBinderGreetingValidator(this.outputDestination);
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
