terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.8.0"
    }
  }
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
  display_name = "env-admin-cluster-api-key"
  description  = "Environment manager cluster API Key"
  owner {
    id          = var.env_admin.id
    api_version = var.env_admin.api_version
    kind        = var.env_admin.kind
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

resource "confluent_kafka_topic" "input-men" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name       = "input.men.avro"
  partitions_count = 1
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "3600000"
  }
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}

resource "confluent_kafka_topic" "input-women" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name       = "input.women.avro"
  partitions_count = 1
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "3600000"
  }
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}

resource "confluent_kafka_topic" "output" {
  kafka_cluster {
    id = confluent_kafka_cluster.basic.id
  }
  topic_name       = "output.avro"
  partitions_count = 1
  rest_endpoint    = confluent_kafka_cluster.basic.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "3600000"
  }
  credentials {
    key    = confluent_api_key.env-admin.id
    secret = confluent_api_key.env-admin.secret
  }
}
