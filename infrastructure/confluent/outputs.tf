output "cluster_poc_user_id" {
  value = module.users.cluster_poc_user_api_key.id
}

output "cluster_poc_user_secret" {
  value     = module.users.cluster_poc_user_api_key.secret
  sensitive = true
}

output "cluster_poc_test_id" {
  value = module.users.cluster_poc_test_api_key.id
}

output "cluster_poc_test_secret" {
  value     = module.users.cluster_poc_test_api_key.secret
  sensitive = true
}

output "schema_registry_poc_user_id" {
  value = module.users.schema_registry_poc_user_api_key.id
}

output "schema_registry_poc_user_secret" {
  value     = module.users.schema_registry_poc_user_api_key.secret
  sensitive = true
}

output "schema_registry_poc_test_id" {
  value = module.users.schema_registry_poc_test_api_key.id
}

output "schema_registry_poc_test_secret" {
  value     = module.users.schema_registry_poc_test_api_key.secret
  sensitive = true
}

output "kafka_broker_url" {
  value = module.cluster.cluster.bootstrap_endpoint
}

output "schema_registry_url" {
  value = module.schema-registry.schema_registry.rest_endpoint
}
