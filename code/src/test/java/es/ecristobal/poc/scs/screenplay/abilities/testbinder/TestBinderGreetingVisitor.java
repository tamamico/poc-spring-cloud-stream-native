package es.ecristobal.poc.scs.screenplay.abilities.testbinder;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;
import es.ecristobal.poc.scs.screenplay.actors.Customer;
import org.springframework.cloud.stream.binder.test.InputDestination;

import static org.springframework.integration.support.MessageBuilder.withPayload;

public class TestBinderGreetingVisitor
        extends GreetingVisitor {

    private final InputDestination inputDestination;

    TestBinderGreetingVisitor(final InputDestination inputDestination) {
        this.inputDestination = inputDestination;
    }

    @Override
    public void visit(final Customer customer) {
        this.inputDestination.send(withPayload(this.buildFrom(customer)).build());
    }

}
