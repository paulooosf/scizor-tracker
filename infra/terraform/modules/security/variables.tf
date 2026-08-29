# =============================================================================
# VARIÁVEIS DO MÓDULO SECURITY
# =============================================================================
# Este módulo cria Security Groups (firewalls) para cada camada da aplicação

variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
}

variable "vpc_id" {
  description = "ID da VPC onde os security groups serão criados"
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block da VPC (para permitir trafego interno)"
  type        = string
}

variable "tags" {
  description = "Tags comuns"
  type        = map(string)
  default     = {}
}
