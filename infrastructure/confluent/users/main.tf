terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

data "confluent_user" "poc-user" {
  email = "ecristobalr@gmail.com"
}

resource "confluent_api_key" "poc-user" {
  display_name = "poc-user"
  description  = "Kafka API Key that is owned by 'poc-user' account"
  owner {
    id          = data.confluent_user.poc-user.id
    api_version = data.confluent_user.poc-user.api_version
    kind        = data.confluent_user.poc-user.kind
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

resource "confluent_kafka_acl" "poc-user-input-topic" {
  kafka_cluster {
    id = var.cluster.id
  }
  resource_type = "TOPIC"
  resource_name = var.input-topic
  pattern_type  = "LITERAL"
  principal     = "User:${data.confluent_user.poc-user.id}"
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
  principal     = "User:${data.confluent_user.poc-user.id}"
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
  pattern_type  = "PREFIXED"
  principal     = "User:${data.confluent_user.poc-user.id}"
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
  principal   = "User:${data.confluent_user.poc-user.id}"
  role_name   = "DeveloperRead"
  crn_pattern = "${var.schema-registry}/subject=*"
}
