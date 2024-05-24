terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

resource "confluent_environment" "development" {
  display_name = "development"
}

resource "confluent_service_account" "cluster-manager" {
  display_name = "cluster-manager"
  description  = "Cluster manager service account created by Terraform"
}

resource "confluent_role_binding" "app-manager-kafka-cluster-admin" {
  principal   = "User:${confluent_service_account.cluster-manager.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_environment.development.resource_name
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

resource "confluent_api_key" "cluster-manager" {
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

module "topics" {
  source = "./topics"

  cluster_id            = confluent_kafka_cluster.basic.id
  cluster_rest_endpoint = confluent_kafka_cluster.basic.rest_endpoint
  api_key_id            = confluent_api_key.cluster-manager.id
  api_key_secret        = confluent_api_key.cluster-manager.secret
}

module "schema-registry" {
  source = "./schema-registry"

  environment = confluent_environment.development.id
}
