variable "environment" {
  type = string
}

variable "api_key" {
  type = object({
    id          = string
    api-version = string
    kind        = string
  })
  sensitive = true
}
