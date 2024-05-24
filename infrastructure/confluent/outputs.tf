output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "schema_registry_url" {
  value = module.schema-registry.schema_registry_url
}

output "men-input-topic-name" {
  value = module.topics.men-input-topic-name
}

output "output-topic-name" {
  value = module.topics.output-topic-name
}
