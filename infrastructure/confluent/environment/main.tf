terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.8.0"
    }
  }
}

resource "confluent_environment" "staging" {
  display_name = "staging"

  stream_governance {
    package = "ESSENTIALS"
  }
}

resource "confluent_service_account" "env-admin" {
  display_name = "env-admin"
  description  = "Environment admin service account created by Terraform"
}

resource "confluent_role_binding" "env-admin" {
  principal   = "User:${confluent_service_account.env-admin.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_environment.staging.resource_name
}
