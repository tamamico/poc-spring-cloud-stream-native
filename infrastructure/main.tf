terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.75.0"
    }
  }
}

resource "confluent_environment" "development" {
  display_name = "Development"

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_service_account" "sa" {
  display_name = "sa"
  description  = "Service account created by Terraform"
}

resource "confluent_kafka_cluster" "basic" {
  display_name = "poc_kafka_cluster"
  availability = "SINGLE_ZONE"
  cloud        = "AWS"
  region       = "eu-south-2"
  basic {}

  environment {
    id = confluent_environment.development.id
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "confluent_api_key" "basic-cluster-api-key" {
  display_name = "api-key"
  description  = "Cluster API Key"
  owner {
    id          = confluent_service_account.sa.id
    api_version = confluent_service_account.sa.api_version
    kind        = confluent_service_account.sa.kind
  }

  managed_resource {
    id          = confluent_kafka_cluster.basic.id
    api_version = confluent_kafka_cluster.basic.api_version
    kind        = confluent_kafka_cluster.basic.kind

    environment {
      id = confluent_environment.development.id
    }
  }

  lifecycle {
    prevent_destroy = true
  }
}

data "confluent_schema_registry_cluster" "essentials" {
  environment {
    id = confluent_environment.development.id
  }

}

data "confluent_schema_registry_cluster_config" "essentials" {
  schema_registry_cluster {
    id = data.confluent_schema_registry_cluster.essentials.id
  }
  rest_endpoint = data.confluent_schema_registry_cluster.essentials.rest_endpoint
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

resource "confluent_kafka_topic" "input-men" {
  kafka_cluster    = confluent_kafka_cluster.basic.id
  topic_name       = "greet.men"
  partitions_count = 4
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

resource "confluent_kafka_topic" "output" {
  kafka_cluster    = confluent_kafka_cluster.basic.id
  topic_name       = "greeting"
  partitions_count = 4
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

resource "confluent_kafka_acl" "input-men" {
  kafka_cluster = confluent_kafka_cluster.basic.id
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.input-men.topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.sa.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

resource "confluent_kafka_acl" "output" {
  kafka_cluster = confluent_kafka_cluster.basic.id
  resource_type = "TOPIC"
  resource_name = confluent_kafka_topic.output.topic_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.sa.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

resource "confluent_kafka_acl" "basic" {
  kafka_cluster = confluent_kafka_cluster.basic.id
  resource_type = "CLUSTER"
  resource_name = confluent_kafka_cluster.basic.display_name
  pattern_type  = "LITERAL"
  principal     = "User:${confluent_service_account.sa.id}"
  host          = "*"
  operation     = "DESCRIBE"
  permission    = "ALLOW"
  rest_endpoint = confluent_kafka_cluster.basic.rest_endpoint
  credentials {
    key    = confluent_api_key.basic-cluster-api-key.id
    secret = confluent_api_key.basic-cluster-api-key.secret
  }
}

output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "schema_registry_url" {
  value = data.confluent_schema_registry_cluster.essentials.rest_endpoint
}

output "men-input-topic-name" {
  value = confluent_kafka_topic.input-men.topic_name
}

output "output-topic-name" {
  value = confluent_kafka_topic.output.topic_name
}
