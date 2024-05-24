output "men-input-topic-name" {
  value = confluent_kafka_topic.input-men.topic_name
}

output "output-topic-name" {
  value = confluent_kafka_topic.output.topic_name
}
