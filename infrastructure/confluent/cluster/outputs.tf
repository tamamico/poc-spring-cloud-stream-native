output "cluster" {
  value = confluent_kafka_cluster.basic
}

output "api_key" {
  value = confluent_api_key.env-admin
}
