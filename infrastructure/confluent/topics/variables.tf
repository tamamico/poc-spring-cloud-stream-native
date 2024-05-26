variable "cluster" {
  type = object({
    id = string
    rest_endpoint = string
  })
}

variable "api_key" {
  type = object({
    id = string
    secret = string
  })
  sensitive = true
}
