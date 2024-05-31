output "cluster" {
  value = confluent_kafka_cluster.basic
}

output "admin-cluster-api-key" {
  value = confluent_api_key.env-admin
}

output "input-men-topic" {
  value = confluent_kafka_topic.input-men
}

output "output-topic" {
  value = confluent_kafka_topic.output
}
