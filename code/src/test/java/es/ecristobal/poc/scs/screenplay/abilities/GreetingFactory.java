package es.ecristobal.poc.scs.screenplay.abilities;

public interface GreetingFactory {

    GreetingVisitorBuilder greetingVisitorBuilder();

    GreetingValidatorBuilder greetingValidatorBuilder();
}
