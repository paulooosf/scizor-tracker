# =============================================================================
# VARIÁVEIS DO MÓDULO ALB
# =============================================================================

variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
}

variable "vpc_id" {
  description = "ID da VPC"
  type        = string
}

variable "public_subnet_ids" {
  description = "Lista de IDs das subnets públicas para o ALB"
  type        = list(string)
}

variable "alb_security_group_id" {
  description = "ID do security group do ALB"
  type        = string
}

# Porta que a aplicação expõe
variable "app_port" {
  description = "Porta da aplicação (Spring Boot)"
  type        = number
  default     = 8080
}

# Health check path
variable "health_check_path" {
  description = "Path para health check"
  type        = string
  default     = "/actuator/health"  # Spring Boot Actuator
}

variable "tags" {
  description = "Tags comuns"
  type        = map(string)
  default     = {}
}
