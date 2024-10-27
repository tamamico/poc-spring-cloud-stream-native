output "schema-registry-url" {
  value = data.confluent_schema_registry_cluster.schema-registry.rest_endpoint
}
