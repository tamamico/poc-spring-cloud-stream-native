terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

resource "confluent_environment" "development" {
  display_name = "Development"

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_service_account" "cluster-manager" {
  display_name = "cluster-manager"
  description  = "Cluster manager service account created by Terraform"
}

resource "confluent_role_binding" "app-manager-kafka-cluster-admin" {
  principal   = "User:${confluent_service_account.cluster-manager.id}"
  role_name   = "CloudClusterAdmin"
  crn_pattern = confluent_kafka_cluster.basic.rbac_crn
}

resource "confluent_kafka_cluster" "basic" {
  display_name = "poc_kafka_cluster"
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = "us-east-1"
  basic {}
  environment {
    id = confluent_environment.development.id
  }
}

resource "confluent_api_key" "cluster" {
  display_name = "cluster-api-key"
  description  = "Cluster API Key"
  owner {
    id          = confluent_service_account.cluster-manager.id
    api_version = confluent_service_account.cluster-manager.api_version
    kind        = confluent_service_account.cluster-manager.kind
  }
  managed_resource {
    id          = confluent_kafka_cluster.basic.id
    api_version = confluent_kafka_cluster.basic.api_version
    kind        = confluent_kafka_cluster.basic.kind
    environment {
      id = confluent_environment.development.id
    }
  }
}

data "confluent_schema_registry_cluster" "essentials" {
  environment {
    id = confluent_environment.development.id
  }

}

resource "confluent_kafka_topic" "input-men" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name       = "greet.men"
  partitions_count = 4
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = confluent_api_key.cluster.id
    secret = confluent_api_key.cluster.secret
  }
}

resource "confluent_kafka_topic" "output" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name       = "greeting"
  partitions_count = 4
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = confluent_api_key.cluster.id
    secret = confluent_api_key.cluster.secret
  }
}
