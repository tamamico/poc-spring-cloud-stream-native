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
