---
title: Source code
---
# Business logic

[Funcional requirements](https://app.swimm.io/workspaces/45Dj0avbXuJxVOlOtI1M/repos/Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=/branch/main/docs/yfy9g8ol/edit#functional) show that this PoC have a very simple business logic, given that basically it only have to change to uppercase letters the incoming name and use it in a greeting.

However, the second [non-functional requirement](https://app.swimm.io/workspaces/45Dj0avbXuJxVOlOtI1M/repos/Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=/branch/main/docs/yfy9g8ol/edit#non-functional) states that we should use Avro as message format, which means we won't receive nor send a String, but rather Java classes mapping these Avro messages. Later we will see how did we manage this part, but for now is enough to know that both input and output messages are mapped to 2 classes called <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" pos="3:12:12" line-data="import es.ecristobal.poc.scs.avro.Input;">`Input`</SwmToken> and <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" pos="4:12:12" line-data="import es.ecristobal.poc.scs.avro.Output;">`Output`</SwmToken>, respectively.

With both things in mind, the code needed to implement the business logic is the following one:

<SwmSnippet path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" line="18">

---

Method implementing business logic for our PoC, using classes wrapping input and output Avro messages

```java
    Mono<Output> greet(
            final Input input
    ) {
        return just(input.getWho()).doOnNext(name -> log.info("Received person name to greet"))
                                   .map(name -> newBuilder().setMessage(format("Hello, %S!", name))
                                                            .setDate(valueOf(now().atStartOfDay()).getNanos())
                                                            .build());
    }
```

---

</SwmSnippet>

# Avro messages

We can follow a 3-step process to handle our input and output messages easily inside our application:

- Define Avro schemas for both input and output messages

<SwmSnippet path="/code/src/main/avro/input.avsc" line="1">

---

Avro schema for input message

```avsc
{
    "type": "record",
    "name": "Input",
    "namespace": "es.ecristobal.poc.scs.avro",
    "fields": [
        {
            "name": "who",
            "type": "string"
        }
    ]
}
```

---

</SwmSnippet>

<SwmSnippet path="/code/src/main/avro/output.avsc" line="1">

---

Avro schema for output message

```avsc
{
    "type": "record",
    "name": "Output",
    "namespace": "es.ecristobal.poc.scs.avro",
    "fields": [
        {
            "name": "date",
            "type": "int",
            "logicalType": "date"
        },
        {
            "name": "message",
            "type": "string"
        }
    ]
}
```

---

</SwmSnippet>

- Use <SwmToken path="/code/pom.xml" pos="194:4:8" line-data="                &lt;artifactId&gt;avro-maven-plugin&lt;/artifactId&gt;">`avro-maven-plugin`</SwmToken> to auto-generate the Java classes mapping aforementioned Avro schema

<SwmSnippet path="/code/pom.xml" line="192">

---

Maven plugin to generate Java classes from Avro schemas

```xml
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>${avro-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <id>schemas</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>schema</goal>
                            <goal>protocol</goal>
                            <goal>idl-protocol</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <outputDirectory>${project.build.directory}/generated-sources</outputDirectory>
                </configuration>
            </plugin>
```

---

</SwmSnippet>

- Set-up Kafka with Avro serialization

<SwmSnippet path="/code/pom.xml" line="87">

---

Maven artifact with Kafka Avro serializers

```xml
        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-avro-serializer</artifactId>
            <version>${kafka-avro-serializer.version}</version>
        </dependency>
```

---

</SwmSnippet>

<SwmSnippet path="/code/src/main/resources/application.properties" line="19">

---

Deserializer for incoming messages

```
spring.cloud.stream.kafka.binder.consumer-properties.value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
```

---

</SwmSnippet>

<SwmSnippet path="/code/src/main/resources/application.properties" line="25">

---

Serializer for outgoing messages

```
spring.cloud.stream.kafka.binder.producer-properties.value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
```

---

</SwmSnippet>

# Spring Cloud Stream set-up

We have a very straightforward set-up for our application, adding only some complexity to comply with sixth and seventh non-functional requirements.

First of all, we have the Spring Boot application class, whose only particularity is the addition of <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/StreamConfiguration.java" pos="34:1:3" line-data="        enableAutomaticContextPropagation();">`enableAutomaticContextPropagation()`</SwmToken> method call to propagate span and trace IDs:

<SwmSnippet path="/code/src/main/java/es/ecristobal/poc/scs/GreeterApplication.java" line="1">

---

Spring Boot application class

```java
package es.ecristobal.poc.scs;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
public class GreeterApplication {

    public static void main(String[] args) {
        run(GreeterApplication.class, args);
    }

}
```

---

</SwmSnippet>

The next step is to configure Spring Cloud Stream, for which we need to add the required dependencies and, then, set-up the stream on a Spring bean:

<SwmSnippet path="/code/pom.xml" line="77">

---

Maven dependency for [Reactive Kafka binder](https://docs.spring.io/spring-cloud-stream/reference/kafka/kafka-reactive-binder/overview.html)

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-stream-binder-kafka-reactive</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>commons-logging</groupId>
                    <artifactId>commons-logging</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
```

---

</SwmSnippet>

<SwmSnippet path="code/src/main/java/es/ecristobal/poc/scs/StreamConfiguration.java" line="28">

---

Bean setting up the stream (including <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" pos="21:18:18" line-data="        return just(input.getWho()).doOnNext(name -&gt; log.info(&quot;Received person name to greet&quot;))">`log`</SwmToken>)

```java

    private static final String KAFKA_KEY = "kafka_receivedMessageKey";
    private static final String USER_NAME = "user.name";

    @PostConstruct
    public void init() {
        enableAutomaticContextPropagation();
    }

    @Bean
    Greeter greeter() {
        return new Greeter();
    }

    @Bean
    Function<Flux<Message<Input>>, Flux<Message<Output>>> greet(
            final Greeter greeter
    ) {
        getInstance().registerThreadLocalAccessor(USER_NAME, () -> MDC.get(USER_NAME),
                                                  username -> MDC.put(USER_NAME, username),
                                                  () -> MDC.remove(USER_NAME));
        return flux -> flux.flatMap(message -> {
            final Input input = message.getPayload();
            ofNullable(message.getHeaders()
                              .get(ACKNOWLEDGMENT, ReceiverOffset.class)).ifPresent(ReceiverOffset::acknowledge);
            return greeter.greet(input)
                          .contextWrite(of(USER_NAME, input.getWho()
                                                           .toString()))
                          .map(output -> withPayload(output).setHeader(KEY, message.getHeaders()
                                                                                   .get(KAFKA_KEY))
                                                            .build());

        });
    }
```

---

</SwmSnippet>

Finally, to make everything work we have to add all required configuration in <SwmPath>[code/…/resources/application.properties](/code/src/main/resources/application.properties)</SwmPath>:

<SwmSnippet path="/code/src/main/resources/application.properties" line="9">

---

Spring Cloud Stream configuration

```
spring.cloud.stream.bindings.greet-in-0.group=${spring.application.name}
spring.cloud.stream.bindings.greet-in-0.destination=input.men.avro,input.women.avro
spring.cloud.stream.bindings.greet-in-0.consumer.use-native-decoding=true
spring.cloud.stream.bindings.greet-out-0.destination=output.avro
spring.cloud.stream.bindings.greet-out-0.producer.use-native-encoding=true
spring.cloud.stream.kafka.binder.auto-create-topics=false
spring.cloud.stream.kafka.binder.consumer-properties.specific.avro.reader=true
spring.cloud.stream.kafka.binder.consumer-properties.basic.auth.credentials.source=USER_INFO
spring.cloud.stream.kafka.binder.consumer-properties.basic.auth.user.info=${schema-registry.user}:${schema-registry.password}
spring.cloud.stream.kafka.binder.consumer-properties.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.cloud.stream.kafka.binder.consumer-properties.value.deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer
spring.cloud.stream.kafka.binder.consumer-properties.value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy
spring.cloud.stream.kafka.binder.producer-properties.auto.register.schemas=false
spring.cloud.stream.kafka.binder.producer-properties.basic.auth.credentials.source=USER_INFO
spring.cloud.stream.kafka.binder.producer-properties.basic.auth.user.info=${schema-registry.user}:${schema-registry.password}
spring.cloud.stream.kafka.binder.producer-properties.key.serializer=org.apache.kafka.common.serialization.StringSerializer
spring.cloud.stream.kafka.binder.producer-properties.value.serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
spring.cloud.stream.kafka.binder.producer-properties.value.subject.name.strategy=io.confluent.kafka.serializers.subject.RecordNameStrategy
spring.cloud.stream.kafka.binder.configuration.metadata.max.age.ms=1000
spring.cloud.stream.kafka.binder.configuration.security.protocol=SASL_SSL
spring.cloud.stream.kafka.binder.configuration.sasl.mechanism=PLAIN
spring.cloud.stream.kafka.binder.configuration.sasl.jaas.config=\
  ${kafka.login.module:org.apache.kafka.common.security.plain.PlainLoginModule} \
  required username='${kafka.user}' password='${kafka.password}';
spring.cloud.stream.kafka.binder.enable-observation=true
spring.cloud.stream.kafka.bindings.greet-in-0.consumer.standard-headers=both
spring.cloud.stream.kafka.bindings.greet-in-0.consumer.start-offset=latest
```

---

</SwmSnippet>

Bear in mind that this configuration do not include environment-specific settings for safety reasons, but we can provide it through the following environment variables:

| Name                                                               | Description                              |
| ------------------------------------------------------------------ | ---------------------------------------- |
| KAFKA_USER                                                         | Username to connect with Kafka broker    |
| KAFKA_PASSWORD                                                     | Password to connect with Kafka broker    |
| SCHEMAREGISTRY_USER                                                | Username to connect with Schema Registry |
| SCHEMAREGISTRY_PASSWORD                                            | Password to connect with Schema Registry |
| SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS                           | Kafka broker URL                         |
| SPRING_CLOUD_STREAM_KAFKA_BINDER_CONFIGURATION_SCHEMA_REGISTRY_URL | Schema registry URL                      |

<SwmMeta version="3.0.0" repo-id="Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=" repo-name="poc-spring-cloud-stream-native"><sup>Powered by [Swimm](https://app.swimm.io/)</sup></SwmMeta>
