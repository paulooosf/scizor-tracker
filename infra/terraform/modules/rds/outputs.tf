# =============================================================================
# OUTPUTS DO MÓDULO RDS
# =============================================================================

# Endpoint de conexão do RDS
# Formato: nome-da-instancia.xxxxx.regiao.rds.amazonaws.com:5432
output "db_endpoint" {
  description = "Endpoint de conexão do RDS PostgreSQL"
  value       = aws_db_instance.main.endpoint
}

# Endereço (sem a porta)
output "db_address" {
  description = "Endereço do RDS (sem porta)"
  value       = aws_db_instance.main.address
}

# Porta
output "db_port" {
  description = "Porta do PostgreSQL"
  value       = aws_db_instance.main.port
}

# Nome do database
output "db_name" {
  description = "Nome do database"
  value       = aws_db_instance.main.db_name
}

# Username
output "db_username" {
  description = "Username master do PostgreSQL"
  value       = aws_db_instance.main.username
  sensitive   = true
}

# ARN da instância
output "db_instance_arn" {
  description = "ARN da instância RDS"
  value       = aws_db_instance.main.arn
}

# ID da instância
output "db_instance_id" {
  description = "ID da instância RDS"
  value       = aws_db_instance.main.id
}
