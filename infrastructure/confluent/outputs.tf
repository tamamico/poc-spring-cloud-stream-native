output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "schema_registry_url" {
  value = confluent_schema_registry_cluster.kafka.rest_endpoint
}
