package es.ecristobal.poc.scs.screenplay.abilities;

import java.util.function.Consumer;

public interface GreetingValidator {

    void with(final Consumer<String> assertions);
}
