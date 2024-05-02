package es.ecristobal.poc.scs.screenplay.interactions.send;

import es.ecristobal.poc.scs.screenplay.actors.Customer;

public interface GreetingVisitor {

    void visit(final Customer customer);
}
