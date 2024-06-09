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

resource "confluent_service_account" "env-admin" {
  display_name = "env-admin"
  description  = "Environment admin service account created by Terraform"
}

resource "confluent_role_binding" "env-admin" {
  principal   = "User:${confluent_service_account.env-admin.id}"
  role_name   = "EnvironmentAdmin"
  crn_pattern = confluent_environment.development.resource_name
}

module "cluster" {
  source = "./cluster"

  environment = confluent_environment.development.id
  env_admin = {
    id          = confluent_service_account.env-admin.id
    api_version = confluent_service_account.env-admin.api_version
    kind        = confluent_service_account.env-admin.kind
  }
}

module "schema-registry" {
  source = "./schema-registry"

  environment = confluent_environment.development.id
  env_admin = {
    id          = confluent_service_account.env-admin.id
    api_version = confluent_service_account.env-admin.api_version
    kind        = confluent_service_account.env-admin.kind
  }
}

module "users" {
  source = "./users"

  environment = confluent_environment.development.id
  cluster = {
    id            = module.cluster.cluster.id
    name          = module.cluster.cluster.display_name
    api_version   = module.cluster.cluster.api_version
    kind          = module.cluster.cluster.kind
    rest_endpoint = module.cluster.cluster.rest_endpoint
  }
  schema_registry = {
    id          = module.schema-registry.schema_registry.id
    name        = module.schema-registry.schema_registry.display_name
    api_version = module.schema-registry.schema_registry.api_version
    kind        = module.schema-registry.schema_registry.kind
  }
  api_key = {
    id     = module.cluster.admin-cluster-api-key.id
    secret = module.cluster.admin-cluster-api-key.secret
  }
  input-men-topic   = module.cluster.input-men-topic.topic_name
  input-women-topic = module.cluster.input-women-topic.topic_name
  output-topic      = module.cluster.output-topic.topic_name
  schema-registry   = module.schema-registry.schema_registry.resource_name
}
