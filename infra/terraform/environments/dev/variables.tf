# =============================================================================
# VARIÁVEIS DO AMBIENTE DEV
# =============================================================================

variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome do projeto"
  type        = string
  default     = "scizor-tracker"
}

variable "environment" {
  description = "Ambiente"
  type        = string
  default     = "dev"
}

# Networking
variable "vpc_cidr" {
  description = "CIDR da VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability Zones"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "public_subnet_cidrs" {
  description = "CIDRs das subnets públicas"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDRs das subnets privadas"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

# RDS
variable "db_name" {
  description = "Nome do database"
  type        = string
  default     = "scizor_tracker"
}

variable "db_username" {
  description = "Username do PostgreSQL"
  type        = string
  default     = "scizor_admin"
}

variable "db_password" {
  description = "Senha do PostgreSQL"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "Classe da instância RDS"
  type        = string
  default     = "db.t3.micro"  # Free tier
}

variable "allocated_storage" {
  description = "Storage em GB"
  type        = number
  default     = 20  # Free tier
}

variable "backup_retention_period" {
  description = "Dias de retencao de backups (0 = desabilitado para free tier)"
  type        = number
  default     = 0
}

# ECS
variable "docker_image" {
  description = "Imagem Docker da aplicação"
  type        = string
  # IMPORTANTE: Você precisa fazer push da imagem para Docker Hub ou ECR
  # Exemplo: "seu-usuario/scizor-tracker:latest"
  # Ou ECR: "123456789012.dkr.ecr.us-east-1.amazonaws.com/scizor-tracker:latest"
}
