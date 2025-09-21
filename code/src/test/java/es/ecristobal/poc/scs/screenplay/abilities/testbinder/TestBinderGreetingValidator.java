package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import java.io.IOException;

import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;
import org.springframework.cloud.stream.binder.test.OutputDestination;

import static java.nio.ByteBuffer.wrap;

import static es.ecristobal.poc.scs.avro.Output.fromByteBuffer;
import static org.junit.jupiter.api.Assertions.fail;

public class TestBinderGreetingValidator
        implements GreetingValidator {

    private final OutputDestination outputDestination;

    @Builder
    private TestBinderGreetingValidator(final OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        try {
            final Output output = fromByteBuffer(wrap(this.outputDestination.receive()
                                                                            .getPayload()));
            assertions.accept(output.getMessage()
                                    .toString());
        } catch(IOException e) {
            fail(e);
        }

    }

}
