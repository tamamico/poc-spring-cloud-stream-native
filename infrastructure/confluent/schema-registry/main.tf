terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

data "confluent_schema_registry_cluster" "kafka" {
  environment {
    id = var.environment
  }
}
