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
  description  = "Service account for PoC testing"
}

resource "confluent_api_key" "poc-user" {
  display_name = "poc-user"
  description  = "Kafka API Key that is owned by 'poc-user' account"
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

resource "confluent_kafka_acl" "poc-user-consumer-group" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "GROUP"
  resource_name = "poc"
  pattern_type  = "PREFIXED"
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

resource "confluent_role_binding" "schema-registry" {
  principal   = "User:${confluent_service_account.poc-user.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${var.schema-registry}/subject=es.ecristobal.poc.scs.avro.*"
}
