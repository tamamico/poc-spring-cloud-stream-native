package es.ecristobal.poc.scs.screenplay.actors;

import es.ecristobal.poc.scs.screenplay.abilities.GreetingVisitor;

public record Customer(String name) {

    public void accept(GreetingVisitor visitor) {
        visitor.visit(this);
    }
}
