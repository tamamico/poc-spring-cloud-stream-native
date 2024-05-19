package es.ecristobal.poc.scs.screenplay.abilities;

public interface GreetingFactory {

    GreetingVisitor greetingVisitor();

    GreetingValidator greetingValidator();
}
