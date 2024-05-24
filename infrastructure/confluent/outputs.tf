output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}
