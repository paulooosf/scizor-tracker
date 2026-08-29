# =============================================================================
# OUTPUTS DO MÓDULO SECURITY
# =============================================================================

# Security Group do ALB
output "alb_security_group_id" {
  description = "ID do security group do ALB"
  value       = aws_security_group.alb.id
}

# Security Group das ECS Tasks
output "ecs_tasks_security_group_id" {
  description = "ID do security group das ECS tasks"
  value       = aws_security_group.ecs_tasks.id
}

# Security Group do RDS
output "rds_security_group_id" {
  description = "ID do security group do RDS"
  value       = aws_security_group.rds.id
}

# Security Group dos VPC Endpoints
output "vpc_endpoints_security_group_id" {
  description = "ID do security group dos VPC endpoints"
  value       = aws_security_group.vpc_endpoints.id
}
