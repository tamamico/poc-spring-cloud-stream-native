terraform {
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "1.76.0"
    }
  }
}

resource "confluent_kafka_topic" "input-men" {
  kafka_cluster {
    id = var.cluster.id
  }
  topic_name       = "greet.men"
  partitions_count = 1
  rest_endpoint    = var.cluster.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}

resource "confluent_kafka_topic" "output" {
  kafka_cluster {
    id = var.cluster.id
  }
  topic_name       = "greeting"
  partitions_count = 1
  rest_endpoint    = var.cluster.rest_endpoint
  config = {
    "cleanup.policy" = "compact"
    "retention.ms"   = "86400000"
  }
  credentials {
    key    = var.api_key.id
    secret = var.api_key.secret
  }
}
