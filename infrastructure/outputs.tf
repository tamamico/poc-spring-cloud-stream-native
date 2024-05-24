output "kafka_broker_address" {
  value = confluent_kafka_cluster.basic.bootstrap_endpoint
}

output "schema_registry_url" {
  value = confluent_schema_registry_cluster.kafka.rest_endpoint
}
/*
output "men-input-topic-name" {
  value = confluent_kafka_topic.input-men.topic_name
}

output "output-topic-name" {
  value = confluent_kafka_topic.output.topic_name
}
*/
