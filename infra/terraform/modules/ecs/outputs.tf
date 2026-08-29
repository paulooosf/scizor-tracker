# =============================================================================
# OUTPUTS DO MÓDULO ECS
# =============================================================================

# ID do Cluster
output "cluster_id" {
  description = "ID do ECS Cluster"
  value       = aws_ecs_cluster.main.id
}

# Nome do Cluster
output "cluster_name" {
  description = "Nome do ECS Cluster"
  value       = aws_ecs_cluster.main.name
}

# ARN do Service
output "service_arn" {
  description = "ARN do ECS Service"
  value       = aws_ecs_service.app.id
}

# Nome do Service
output "service_name" {
  description = "Nome do ECS Service"
  value       = aws_ecs_service.app.name
}

# ARN da Task Definition
output "task_definition_arn" {
  description = "ARN da Task Definition"
  value       = aws_ecs_task_definition.app.arn
}

# Nome do CloudWatch Log Group
output "log_group_name" {
  description = "Nome do CloudWatch Log Group"
  value       = aws_cloudwatch_log_group.ecs.name
}

# ARN do Task Execution Role
output "task_execution_role_arn" {
  description = "ARN do IAM Role para task execution"
  value       = aws_iam_role.ecs_task_execution.arn
}

# ARN do Task Role
output "task_role_arn" {
  description = "ARN do IAM Role para a task em execução"
  value       = aws_iam_role.ecs_task.arn
}
