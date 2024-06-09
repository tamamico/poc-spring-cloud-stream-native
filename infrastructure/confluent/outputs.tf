output "poc_user_id" {
  value = module.users.poc_user_api_key.id
}

output "poc_user_secret" {
  value     = module.users.poc_user_api_key.secret
  sensitive = true
}

output "poc_test_id" {
  value = module.users.poc_test_api_key.id
}

output "poc_test_secret" {
  value     = module.users.poc_test_api_key.secret
  sensitive = true
}

output "admin_cluster_id" {
  value = module.cluster.admin-cluster-api-key.id
}

output "admin_cluster_secret" {
  value     = module.cluster.admin-cluster-api-key.secret
  sensitive = true
}

output "kafka_broker_url" {
  value = module.cluster.cluster.bootstrap_endpoint
}

output "schema_registry_url" {
  value = module.schema-registry.schema_registry.rest_endpoint
}

output "men-input-topic-name" {
  value = module.cluster.input-men-topic.topic_name
}

output "women-input-topic-name" {
  value = module.cluster.input-women-topic.topic_name
}

output "output-topic-name" {
  value = module.cluster.output-topic.topic_name
}
