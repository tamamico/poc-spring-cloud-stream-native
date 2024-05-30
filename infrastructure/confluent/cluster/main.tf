terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

resource "confluent_service_account" "env-admin" {
  display_name = "env-admin"
  description  = "Environment admin service account created by Terraform"
}

resource "confluent_role_binding" "env-admin" {
  principal   = "User:${confluent_service_account.env-admin.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_kafka_cluster.basic.rbac_crn
}

resource "confluent_kafka_cluster" "basic" {
  display_name = "poc_kafka_cluster"
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = "us-east-1"
  basic {}
  environment {
    id = var.environment
  }
}

resource "confluent_api_key" "env-admin" {
  display_name = "env-admin-api-key"
  description  = "Environment manager API Key"
  owner {
    id          = confluent_service_account.env-admin.id
    api_version = confluent_service_account.env-admin.api_version
    kind        = confluent_service_account.env-admin.kind
  }
  managed_resource {
    id          = confluent_kafka_cluster.basic.id
    api_version = confluent_kafka_cluster.basic.api_version
    kind        = confluent_kafka_cluster.basic.kind
    environment {
      id = var.environment
    }
  }
}
