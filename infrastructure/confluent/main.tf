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

module "cluster" {
  source = "./cluster"

  environment = confluent_environment.development.id
}

module "topics" {
  source = "./topics"

  cluster = {
    id            = module.cluster.cluster.id
    rest_endpoint = module.cluster.cluster.rest_endpoint
  }
  api_key = {
    id     = module.cluster.api_key.id
    secret = module.cluster.api_key.secret
  }
}

module "schema-registry" {
  source = "./schema-registry"

  environment = confluent_environment.development.id
}

module "users" {
  source = "./users"

  environment = confluent_environment.development.id
  cluster = {
    id          = module.cluster.cluster.id
    api_version = module.cluster.cluster.api_version
    kind        = module.cluster.cluster.kind
  }
  api_key = {
    id     = module.cluster.api_key.id
    secret = module.cluster.api_key.secret
  }
  input-topic     = module.topics.input-men-topic.topic_name
  output-topic    = module.topics.output-topic.topic_name
  schema-registry = module.schema-registry.schema_registry.resource_name
}
