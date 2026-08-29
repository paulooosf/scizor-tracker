# =============================================================================
# VARIÁVEIS DO MÓDULO RDS
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

variable "private_subnet_ids" {
  description = "Lista de IDs das subnets privadas para o RDS"
  type        = list(string)
}

variable "rds_security_group_id" {
  description = "ID do security group do RDS"
  type        = string
}

# Configurações do banco
variable "db_name" {
  description = "Nome do database inicial"
  type        = string
  default     = "scizor_tracker"
}

variable "db_username" {
  description = "Username master do PostgreSQL"
  type        = string
  default     = "scizor_admin"
}

variable "db_password" {
  description = "Senha master do PostgreSQL (será armazenada no Secrets Manager)"
  type        = string
  sensitive   = true  # Não exibe nos logs
}

# Classe da instância RDS
# db.t3.micro = Free tier (750h/mês grátis por 12 meses)
# db.t4g.micro = Mais barato após free tier (~$12/mês)
variable "db_instance_class" {
  description = "Classe da instância RDS"
  type        = string
  default     = "db.t3.micro"  # Free tier
}

# Storage (GB)
# Free tier: até 20GB de SSD (gp2)
variable "allocated_storage" {
  description = "Storage alocado em GB"
  type        = number
  default     = 20  # Free tier
}

# Versão do PostgreSQL
variable "engine_version" {
  description = "Versão do PostgreSQL"
  type        = string
  default     = "15.7"  # Versão estável recente
}

# Backup retention (dias)
# Free tier: até 7 dias de backups
variable "backup_retention_period" {
  description = "Dias para reter backups automáticos"
  type        = number
  default     = 7
}

# Multi-AZ (alta disponibilidade)
# false para dev/free tier
# true para produção (custa o dobro)
variable "multi_az" {
  description = "Habilitar Multi-AZ para alta disponibilidade"
  type        = bool
  default     = false
}

# Janela de manutenção
variable "maintenance_window" {
  description = "Janela de manutenção (UTC)"
  type        = string
  default     = "sun:03:00-sun:04:00"  # Domingo 3-4h da manhã
}

# Janela de backup
variable "backup_window" {
  description = "Janela de backup (UTC)"
  type        = string
  default     = "02:00-03:00"  # 2-3h da manhã (antes da manutenção)
}

# Pular snapshot final ao deletar?
# true para dev (economiza tempo)
# false para prod (segurança)
variable "skip_final_snapshot" {
  description = "Pular snapshot final ao deletar RDS"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags comuns"
  type        = map(string)
  default     = {}
}
