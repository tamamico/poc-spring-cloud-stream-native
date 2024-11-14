---
title: Source code
---
# Business logic

[Funcional requirements](https://app.swimm.io/workspaces/45Dj0avbXuJxVOlOtI1M/repos/Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=/branch/main/docs/yfy9g8ol/edit#functional) show that this PoC have a very simple business logic, given that basically it only have to change to uppercase letters the incoming name and use it in a greeting.

However, the second [non-functional requirement](https://app.swimm.io/workspaces/45Dj0avbXuJxVOlOtI1M/repos/Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=/branch/main/docs/yfy9g8ol/edit#non-functional) states that we should use Avro as message format, which means we won't receive nor send a String, but rather Java classes mapping these Avro messages. Later we will see how did we manage this part, but for now is enough to know that both input and output messages are mapped to 2 classes called <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" pos="3:12:12" line-data="import es.ecristobal.poc.scs.avro.Input;">`Input`</SwmToken> and <SwmToken path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" pos="4:12:12" line-data="import es.ecristobal.poc.scs.avro.Output;">`Output`</SwmToken>, respectively.

With both things in mind, the code needed to implement the business logic is the following one:

<SwmSnippet path="/code/src/main/java/es/ecristobal/poc/scs/Greeter.java" line="10">

---

Method implementing business logic for our PoC, using classes wrapping input and output Avro messages

```java
    Output greet(
            final Input input
    ) {
        return Output.newBuilder()
                     .setMessage(format("Hello, %S!", input.getName()))
                     .build();
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
    "type" : "record",
    "name" : "Input",
    "namespace" : "es.ecristobal.poc.scs.avro",
    "fields" : [
        {
            "name" : "name",
            "type" : "string"
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
    "type" : "record",
    "name" : "Output",
    "namespace" : "es.ecristobal.poc.scs.avro",
    "fields" : [
        {
            "name" : "message",
            "type" : "string"
        }
    ]
}
```

---

</SwmSnippet>

- Use <SwmToken path="/code/pom.xml" pos="164:4:8" line-data="                &lt;artifactId&gt;avro-maven-plugin&lt;/artifactId&gt;">`avro-maven-plugin`</SwmToken> to auto-generate the Java classes mapping aforementioned Avro schema

<SwmSnippet path="/code/pom.xml" line="162">

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

<SwmSnippet path="/code/pom.xml" line="82">

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

<SwmSnippet path="code/src/main/resources/application.yml" line="31">

---

Deserializer for incoming messages

```
            value.deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
```

---

</SwmSnippet>

<SwmSnippet path="/code/src/main/resources/application.yml" line="38">

---

Serializer for outgoing messages

```yaml
            value.serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
```

---

</SwmSnippet>

<SwmMeta version="3.0.0" repo-id="Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=" repo-name="poc-spring-cloud-stream-native"><sup>Powered by [Swimm](https://app.swimm.io/)</sup></SwmMeta>
