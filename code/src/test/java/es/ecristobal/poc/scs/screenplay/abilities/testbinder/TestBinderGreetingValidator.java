package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import java.io.IOException;
import java.nio.ByteBuffer;

import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.test.OutputDestination;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class TestBinderGreetingValidator
        implements GreetingValidator {

    private final OutputDestination outputDestination;

    @Builder
    private TestBinderGreetingValidator(final OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        final byte[] response = this.outputDestination.receive()
                                                      .getPayload();
        log.info("Received response message: \"{}\"", new String(response));
        try {
            final Output output = Output.fromByteBuffer(ByteBuffer.wrap(response));
            assertions.accept(output.getMessage()
                                    .toString());
        } catch(IOException e) {
            fail("Error receiving response message", e);
        }

    }

}
