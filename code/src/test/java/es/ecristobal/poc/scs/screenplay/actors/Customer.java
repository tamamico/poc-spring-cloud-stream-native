package es.ecristobal.poc.scs.screenplay.actors;

import es.ecristobal.poc.scs.screenplay.interactions.send.GreetingVisitor;

public record Customer(String name) {

    public void accept(GreetingVisitor visitor) {
        visitor.visit(this);
    }
}
