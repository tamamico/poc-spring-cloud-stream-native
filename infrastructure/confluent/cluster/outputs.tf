output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "id" {
  value = confluent_kafka_cluster.basic.id
}

output "rest_endpoint" {
  value = confluent_kafka_cluster.basic.rest_endpoint
}

output "api_key_id" {
  value = confluent_api_key.cluster-manager.id
}

output "api_key_secret" {
  value = confluent_api_key.cluster-manager.secret
}
