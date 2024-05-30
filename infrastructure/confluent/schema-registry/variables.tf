variable "environment" {
  type = string
}

variable "env_admin" {
  type = object({
    id          = string
    api-version = string
    kind        = string
  })
  sensitive = true
}
