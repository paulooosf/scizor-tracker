# =============================================================================
# OUTPUTS DO AMBIENTE DEV
# =============================================================================
# Informações importantes após criar a infraestrutura

# -----------------------------------------------------------------------------
# NETWORKING
# -----------------------------------------------------------------------------
output "vpc_id" {
  description = "ID da VPC"
  value       = module.networking.vpc_id
}

output "public_subnet_ids" {
  description = "IDs das subnets públicas"
  value       = module.networking.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs das subnets privadas"
  value       = module.networking.private_subnet_ids
}

# -----------------------------------------------------------------------------
# RDS
# -----------------------------------------------------------------------------
output "rds_endpoint" {
  description = "Endpoint do RDS PostgreSQL"
  value       = module.rds.db_endpoint
}

output "rds_database_name" {
  description = "Nome do database"
  value       = module.rds.db_name
}

# -----------------------------------------------------------------------------
# ALB
# -----------------------------------------------------------------------------
output "alb_dns_name" {
  description = "URL pública da aplicação"
  value       = "http://${module.alb.alb_dns_name}"
}

output "alb_url" {
  description = "URL completa do ALB"
  value       = "http://${module.alb.alb_dns_name}"
}

# -----------------------------------------------------------------------------
# ECS
# -----------------------------------------------------------------------------
output "ecs_cluster_name" {
  description = "Nome do ECS Cluster"
  value       = module.ecs.cluster_name
}

output "ecs_service_name" {
  description = "Nome do ECS Service"
  value       = module.ecs.service_name
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group dos containers"
  value       = module.ecs.log_group_name
}

# -----------------------------------------------------------------------------
# RESUMO
# -----------------------------------------------------------------------------
output "deployment_summary" {
  description = "Resumo do deployment"
  value = <<-EOT
  ================================================================================
  🎉 SCIZOR TRACKER - INFRAESTRUTURA CRIADA COM SUCESSO!
  ================================================================================
  
  📍 Região: ${var.aws_region}
  🏷️  Ambiente: ${var.environment}
  
  🌐 URL da Aplicação:
     http://${module.alb.alb_dns_name}
  
  🗄️  Banco de Dados PostgreSQL:
     Endpoint: ${module.rds.db_endpoint}
     Database: ${module.rds.db_name}
     Username: ${var.db_username}
  
  🐳 ECS Fargate:
     Cluster: ${module.ecs.cluster_name}
     Service: ${module.ecs.service_name}
     Tasks desejadas: 1
  
  📊 Logs:
     CloudWatch: ${module.ecs.log_group_name}
  
  ================================================================================
  ⚙️  PRÓXIMOS PASSOS:
  ================================================================================
  1. Aguarde ~5 minutos para ECS task iniciar
  2. Acesse: http://${module.alb.alb_dns_name}/actuator/health
  3. Verifique logs: aws logs tail ${module.ecs.log_group_name} --follow
  4. Teste a API: http://${module.alb.alb_dns_name}/api/usuarios
  
  💰 CUSTOS ESTIMADOS (após free tier):
     - RDS db.t3.micro: ~$15/mês
     - ECS Fargate (1 task): ~$15/mês
     - ALB: ~$16/mês
     - Total: ~$46/mês
  
  🛡️  SEGURANÇA:
     ✅ RDS em subnet privada
     ✅ Aplicação em subnet privada
     ✅ Security Groups configurados
     ✅ Senha no Secrets Manager
  
  ================================================================================
  EOT
}
