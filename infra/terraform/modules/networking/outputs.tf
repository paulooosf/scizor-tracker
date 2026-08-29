# =============================================================================
# OUTPUTS DO MÓDULO NETWORKING
# =============================================================================
# Outputs são valores que este módulo RETORNA para quem o chamou
# Outros módulos (ECS, RDS, ALB) vão precisar dessas informações

# ID da VPC
output "vpc_id" {
  description = "ID da VPC criada"
  value       = aws_vpc.main.id
}

# CIDR block da VPC
output "vpc_cidr" {
  description = "CIDR block da VPC"
  value       = aws_vpc.main.cidr_block
}

# IDs das subnets públicas (para ALB)
output "public_subnet_ids" {
  description = "Lista de IDs das subnets públicas"
  value       = aws_subnet.public[*].id
}

# IDs das subnets privadas (para ECS tasks e RDS)
output "private_subnet_ids" {
  description = "Lista de IDs das subnets privadas"
  value       = aws_subnet.private[*].id
}

# ID do Internet Gateway
output "internet_gateway_id" {
  description = "ID do Internet Gateway"
  value       = aws_internet_gateway.main.id
}

# Availability Zones usadas
output "availability_zones" {
  description = "Lista de AZs utilizadas"
  value       = var.availability_zones
}
