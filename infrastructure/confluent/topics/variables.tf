variable "cluster_id" {
  type = string
}

variable "cluster_rest_endpoint" {
  type = string
}

variable "api_key_id" {
  type = string
}

variable "api_key_secret" {
  type      = string
  sensitive = true
}
