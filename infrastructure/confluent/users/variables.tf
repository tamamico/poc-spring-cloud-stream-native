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

variable "api_key" {
  type = object({
    id     = string
    secret = string
  })
  sensitive = true
}

variable "input-topic" {
  type = string
}

variable "output-topic" {
  type = string
}

variable "schema-registry" {
  type = string
}
