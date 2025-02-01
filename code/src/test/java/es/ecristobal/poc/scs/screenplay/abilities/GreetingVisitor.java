package es.ecristobal.poc.scs.screenplay.abilities;

import es.ecristobal.poc.scs.avro.Input;
import es.ecristobal.poc.scs.screenplay.actors.Customer;

public abstract class GreetingVisitor {

    public abstract void visit(final Customer customer);

    protected Input buildFrom(final Customer customer) {
        return Input.newBuilder()
                    .setWho(customer.name())
                    .build();
    }

}
