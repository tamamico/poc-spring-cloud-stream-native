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
    id          = module.cluster.cluster.id
    api_version = module.cluster.cluster.api_version
    kind        = module.cluster.cluster.kind
    environment {
      id = var.environment
    }
  }
}

module "topics" {
  source = "./topics"

  cluster = {
    id            = module.cluster.cluster.id
    rest_endpoint = module.cluster.cluster.rest_endpoint
  }
  api_key = {
    id     = confluent_api_key.env_admin.id
    secret = confluent_api_key.env_admin.secret
  }
}

module "schema-registry" {
  source = "./schema-registry"

  environment = confluent_environment.development.id
  api_key = {
    id     = confluent_api_key.env_admin.id
    secret = confluent_api_key.env_admin.secret
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
  api_key = {
    id     = confluent_api_key.env_admin.id
    secret = confluent_api_key.env_admin.secret
  }
  input-topic     = module.topics.input-men-topic.topic_name
  output-topic    = module.topics.output-topic.topic_name
  schema-registry = module.schema-registry.schema_registry.resource_name
}
