variable "environment" {
  type = string
}

variable "env_admin" {
  type = object({
    id          = string
    api_version = string
    kind        = string
  })
  sensitive = true
}
