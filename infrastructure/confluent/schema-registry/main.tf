terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
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
    id          = var.env-admin.id
    api_version = var.env-admin.api_version
    kind        = var.env-admin.kind
  }
  managed_resource {
    id          = module.cluster.cluster.id
    api_version = module.cluster.cluster.api_version
    kind        = module.cluster.cluster.kind
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
  subject_name = "es.ecristobal.poc.scs.avro.Input-value"
  format = "AVRO"
  schema = file("./input.avsc")
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_subject_config" "input" {
  subject_name        = confluent_schema.input.subject_name
  compatibility_level = "FORWARD"
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_schema" "output" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  subject_name = "es.ecristobal.poc.scs.avro.Output-value"
  format = "AVRO"
  schema = file("./output.avsc")
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_subject_config" "output" {
  subject_name        = confluent_schema.output.subject_name
  compatibility_level = "FORWARD"
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}
