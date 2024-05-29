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

resource "confluent_schema" "input" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  subject_name = "es.ecristobal.poc.scs.avro.Input-value"
  format = "AVRO"
  schema = file("./input.avsc")
}

resource "confluent_subject_config" "input" {
  subject_name        = confluent_schema.input.subject_name
  compatibility_level = "FORWARD"
}

resource "confluent_schema" "output" {
  schema_registry_cluster {
    id = confluent_schema_registry_cluster.kafka.id
  }
  rest_endpoint = confluent_schema_registry_cluster.kafka.rest_endpoint
  subject_name = "es.ecristobal.poc.scs.avro.Output-value"
  format = "AVRO"
  schema = file("./output.avsc")
}

resource "confluent_subject_config" "output" {
  subject_name        = confluent_schema.output.subject_name
  compatibility_level = "FORWARD"
}
