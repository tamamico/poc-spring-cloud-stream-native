---
title: Introduction
---
# &nbsp;Purpose

There are 3 main goals behind this PoC:

1. Build an end-to-end example of an enterprise Spring Cloud Stream native application
2. Identify and fix challenges found during development
3. Find and try tools that could simplify, speed up and/or improve our deliverable quality

# Requirements

## Functional

The application will receive as an input a name, and it should output a greeting message for this name, using uppercase letters for it

**Examples:**

- "Steve" -> "Hello, STEVE!"
- Laurene -> "Hello, LAURENE!"

## Non-functional

1. It must use **Kafka as message broker**
2. It must use **Avro as message format**
3. It should be **natively compiled**
4. It should be **delivered as a Docker image**
5. It should be **deployed in a Kubernetes cluster**
6. **Everything as code**
7. It should **include production-ready features**: monitoring, tracing, etc.
8. It should **listen to 2 topics**: one for men greetings and another for women ones
9. If it can't deliver a greeting, the incoming message must be sent to a DLT with contextual information

<SwmMeta version="3.0.0"><sup>Powered by [Swimm](https://app.swimm.io/)</sup></SwmMeta>
