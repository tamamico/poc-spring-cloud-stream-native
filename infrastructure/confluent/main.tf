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

  cluster_id            = module.cluster.cluster.id
  cluster_rest_endpoint = module.cluster.cluster.rest_endpoint
  api_key_id            = module.cluster.api_key.id
  api_key_secret        = module.cluster.api_key.secret
}

module "schema-registry" {
  source = "./schema-registry"

  environment = confluent_environment.development.id
}
