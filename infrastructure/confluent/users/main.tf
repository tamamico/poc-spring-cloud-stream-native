terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

resource "confluent_service_account" "poc-user" {
  display_name = "poc-user"
  description  = "Service account for PoC application"
}

resource "confluent_api_key" "cluster-poc-user" {
  display_name = "cluster-poc-user"
  description  = "Cluster API Key that is owned by 'poc-user' account"
  owner {
    id          = confluent_service_account.poc-user.id
    api_version = confluent_service_account.poc-user.api_version
    kind        = confluent_service_account.poc-user.kind
  }

  managed_resource {
    id          = var.cluster.id
    api_version = var.cluster.api_version
    kind        = var.cluster.kind

    environment {
      id = var.environment
    }
  }
}

resource "confluent_api_key" "schema-registry-poc-user" {
  display_name = "schema-registry-poc-user"
  description  = "Schema Registry API Key that is owned by 'poc-user' account"
  owner {
    id          = confluent_service_account.poc-user.id
    api_version = confluent_service_account.poc-user.api_version
    kind        = confluent_service_account.poc-user.kind
  }

  managed_resource {
    id          = var.schema_registry.id
    api_version = var.schema_registry.api_version
    kind        = var.schema_registry.kind

    environment {
      id = var.environment
    }
  }
}

resource "confluent_service_account" "poc-test" {
  display_name = "poc-test"
  description  = "Service account to test PoC application"
}

resource "confluent_api_key" "cluster-poc-test" {
  display_name = "cluster-poc-test"
  description  = "Cluster API Key that is owned by 'poc-test' account"
  owner {
    id          = confluent_service_account.poc-test.id
    api_version = confluent_service_account.poc-test.api_version
    kind        = confluent_service_account.poc-test.kind
  }

  managed_resource {
    id          = var.cluster.id
    api_version = var.cluster.api_version
    kind        = var.cluster.kind

    environment {
      id = var.environment
    }
  }
}

resource "confluent_api_key" "schema-registry-poc-test" {
  display_name = "schema-registry-poc-test"
  description  = "Schema Registry API Key that is owned by 'poc-test' account"
  owner {
    id          = confluent_service_account.poc-test.id
    api_version = confluent_service_account.poc-test.api_version
    kind        = confluent_service_account.poc-test.kind
  }

  managed_resource {
    id          = var.schema_registry.id
    api_version = var.schema_registry.api_version
    kind        = var.schema_registry.kind

    environment {
      id = var.environment
    }
  }
}

resource "confluent_kafka_acl" "poc-user-input-men-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.input-men-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-user.id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-test-input-men-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.input-men-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-test.id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-user-input-women-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.input-women-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-user.id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-test-input-women-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.input-women-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-test.id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-user-output-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.output-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-user.id}"
  host          = "*"
  operation     = "WRITE"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-test-output-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.output-topic
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-test.id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-user-consumer-group" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "GROUP"
  resource_name = "poc"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-user.id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_acl" "poc-test-consumer-group" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "GROUP"
  resource_name = "greeting-validator"
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.poc-test.id}"
  host          = "*"
  operation     = "READ"
  permission    = "ALLOW"
  rest_endpoint = var.cluster.rest_endpoint
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_role_binding" "poc-user-schema-registry" {
  principal   = "User:${confluent_service_account.poc-user.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${var.schema_registry.resource_name}/subject=es.ecristobal.poc.scs.avro.*"
}

resource "confluent_role_binding" "poc-test-schema-registry" {
  principal   = "User:${confluent_service_account.poc-test.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${var.schema_registry.resource_name}/subject=es.ecristobal.poc.scs.avro.*"
}
