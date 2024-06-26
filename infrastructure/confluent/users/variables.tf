variable "environment" {
  type = string
}

variable "cluster" {
  type = object({
    id            = string
    name          = string
    api_version   = string
    kind          = string
    rest_endpoint = string
  })
}

variable "schema_registry" {
  type = object({
    id            = string
    resource_name = string
    api_version   = string
    kind          = string
  })
}

variable "api_key" {
  type = object({
    id     = string
    secret = string
  })
  sensitive = true
}

variable "input-men-topic" {
  type = string
}

variable "input-women-topic" {
  type = string
}

variable "output-topic" {
  type = string
}
