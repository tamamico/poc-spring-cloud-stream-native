output "cluster_poc_user_api_key" {
  value = confluent_api_key.cluster-poc-user
}

output "cluster_poc_test_api_key" {
  value = confluent_api_key.cluster-poc-user
}

output "schema_registry_poc_user_api_key" {
  value = confluent_api_key.schema-registry-poc-user
}

output "schema_registry_poc_test_api_key" {
  value = confluent_api_key.schema-registry-poc-test
}
