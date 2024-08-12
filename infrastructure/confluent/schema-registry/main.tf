terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.83.0"
    }
  }
}

data "confluent_schema_registry_region" "essentials" {
  cloud   = "AWS"
  region  = "us-east-1"
  package = "ESSENTIALS"
}

resource "confluent_schema_registry_cluster" "kafka" {
  package = data.confluent_schema_registry_region.essentials.package

  environment {
    id = var.environment
  }

  region {
    id = data.confluent_schema_registry_region.essentials.id
  }
}

resource "confluent_api_key" "env-admin" {
  display_name = "env-admin-schema-registry-api-key"
  description  = "Environment manager schema registry API Key"
  owner {
    id          = var.env_admin.id
    api_version = var.env_admin.api_version
    kind        = var.env_admin.kind
  }
  managed_resource {
    id          = confluent_schema_registry_cluster.kafka.id
    api_version = confluent_schema_registry_cluster.kafka.api_version
    kind        = confluent_schema_registry_cluster.kafka.kind
    environment {
      id = var.environment
    }
  }
}

resource "confluent_schema" "input" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  subject_name = "es.ecristobal.poc.scs.avro.Input"
  format = "AVRO"
  schema = file("./input.avsc")
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}

resource "confluent_subject_config" "input" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  subject_name        = confluent_schema.input.subject_name
  compatibility_level = "FORWARD_TRANSITIVE"
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}

resource "confluent_schema" "output" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  subject_name = "es.ecristobal.poc.scs.avro.Output"
  format = "AVRO"
  schema = file("./output.avsc")
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}

resource "confluent_subject_config" "output" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  subject_name        = confluent_schema.output.subject_name
  compatibility_level = "FORWARD_TRANSITIVE"
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}
