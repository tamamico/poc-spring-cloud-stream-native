---
title: Spring Cloud Stream native PoC
---
# &nbsp;Purpose

There are 3 main goals behind this PoC:

1. Build an end-to-end example of an enterprise Spring Cloud Stream native application
2. Identify and fix challenges found during development
3. Find and try tools that could simplify, speed up and/or improve our deliverable quality

# Requirements

## Functional

The application will receive as an input a name, and it should output a greeting message for this name, using uppercase letters for it

**Examples:**&nbsp;

- "Steve" -> "Hello, STEVE!"
- Laurene -> "Hello, LAURENE!"

## Non-functional

- It must use **Kafka as message broker**
- It must use **Avro as message format**
- It should be **natively compiled**
- It should be **delivered as a Docker image**
- It should be **deployed in a Kubernetes cluster**
- **Everything as code**
- It should **include production-ready features**: monitoring, tracing, etc.
- It should **listen to 2 topics**: one for men greetings and another for women ones
- If it can't deliver a greeting, the incoming message must be sent to a DLT with contextual information

##

<SwmMeta version="3.0.0" repo-id="Z2l0aHViJTNBJTNBcG9jLXNwcmluZy1jbG91ZC1zdHJlYW0tbmF0aXZlJTNBJTNBdGFtYW1pY28=" repo-name="poc-spring-cloud-stream-native"><sup>Powered by [Swimm](https://app.swimm.io/)</sup></SwmMeta>
