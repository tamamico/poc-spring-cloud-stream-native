package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.ecristobal.poc.scs.avro.Output;
import es.ecristobal.poc.scs.screenplay.abilities.GreetingValidator;
import lombok.Builder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.messaging.Message;

public class TestBinderGreetingValidator
        implements GreetingValidator {

    private static final Pattern RESPONSE = Pattern.compile("^.*payload=(.*), headers.*");

    private final OutputDestination outputDestination;

    @Builder
    private TestBinderGreetingValidator(final OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    @Override
    public void with(final java.util.function.Consumer<String> assertions) {
        final Output output = this.getFrom(this.outputDestination.receive());
        assertions.accept(output.getMessage()
                                .toString());
    }

    private Output getFrom(final Message<byte[]> message) {
        final Matcher matcher = RESPONSE.matcher(message.toString());
        if(matcher.matches()) {
            try {
                final String              payload = matcher.group(1);
                final DatumReader<Output> reader  = new SpecificDatumReader<>(Output.getClassSchema());
                final Decoder decoder = DecoderFactory.get()
                                                      .jsonDecoder(Output.getClassSchema(), payload);
                return reader.read(null, decoder);
            } catch(IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            throw new IllegalArgumentException("Response message does not match expected pattern");
        }
    }

}
