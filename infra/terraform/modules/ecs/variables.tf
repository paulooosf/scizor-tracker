# =============================================================================
# VARIÁVEIS DO MÓDULO ECS
# =============================================================================

variable "project_name" {
  description = "Nome do projeto"
  type        = string
}

variable "environment" {
  description = "Ambiente (dev, staging, prod)"
  type        = string
}

variable "region" {
  description = "Região AWS"
  type        = string
}

variable "vpc_id" {
  description = "ID da VPC"
  type        = string
}

variable "private_subnet_ids" {
  description = "Lista de IDs das subnets privadas para ECS tasks"
  type        = list(string)
}

variable "ecs_tasks_security_group_id" {
  description = "ID do security group das ECS tasks"
  type        = string
}

variable "target_group_arn" {
  description = "ARN do Target Group do ALB"
  type        = string
}

# Imagem Docker da aplicação
# Pode ser no Docker Hub ou ECR (Elastic Container Registry)
variable "docker_image" {
  description = "Imagem Docker da aplicação"
  type        = string
}

# Recursos do container
# Free tier: 0.5 vCPU, 1GB RAM (por task)
variable "cpu" {
  description = "CPU units para o container (256 = 0.25 vCPU, 512 = 0.5 vCPU, 1024 = 1 vCPU)"
  type        = number
  default     = 512  # 0.5 vCPU
}

variable "memory" {
  description = "Memória em MB (512, 1024, 2048)"
  type        = number
  default     = 1024  # 1GB
}

# Porta da aplicação
variable "container_port" {
  description = "Porta que o container expõe"
  type        = number
  default     = 8080
}

# Número de tasks desejadas
variable "desired_count" {
  description = "Número de tasks ECS desejadas"
  type        = number
  default     = 1  # 1 para dev/free tier
}

# Variáveis de ambiente da aplicação
variable "environment_variables" {
  description = "Variáveis de ambiente para o container"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

# Secrets (credenciais sensíveis do Secrets Manager)
variable "secrets" {
  description = "Secrets do AWS Secrets Manager"
  type = list(object({
    name      = string
    valueFrom = string  # ARN do secret
  }))
  default = []
}

# Auto scaling (opcional)
variable "enable_autoscaling" {
  description = "Habilitar auto scaling"
  type        = bool
  default     = false
}

variable "min_capacity" {
  description = "Mínimo de tasks (auto scaling)"
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Máximo de tasks (auto scaling)"
  type        = number
  default     = 3
}

variable "assign_public_ip" {
  description = "Atribuir IP público às tasks (necessário para acessar ECR sem VPC Endpoint)"
  type        = bool
  default     = false
}

variable "tags" {
  description = "Tags comuns"
  type        = map(string)
  default     = {}
}
