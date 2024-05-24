output "schema_registry_url" {
  value = confluent_schema_registry_cluster.kafka.rest_endpoint
}
