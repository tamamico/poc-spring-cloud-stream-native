package es.ecristobal.poc.scs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestScsNativeApplication {

    public static void main(String[] args) {
        SpringApplication.from(ScsNativeApplication::main).with(TestScsNativeApplication.class).run(args);
    }

}
