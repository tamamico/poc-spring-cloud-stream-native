variable "environment" {
  type = string
}

variable "api_key" {
  type = object({
    id = string
    secret = string
  })
  sensitive = true
}
