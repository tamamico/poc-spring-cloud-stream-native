variable "environment" {
  type = string
}

variable "admin" {
  type = object({
    id = string
    api_version = string
    kind = string
  })
}
