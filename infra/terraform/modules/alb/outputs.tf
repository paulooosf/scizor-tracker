# =============================================================================
# OUTPUTS DO MÓDULO ALB
# =============================================================================

# DNS name do ALB (URL pública para acessar aplicação)
output "alb_dns_name" {
  description = "DNS name do Application Load Balancer"
  value       = aws_lb.main.dns_name
}

# ARN do ALB
output "alb_arn" {
  description = "ARN do Application Load Balancer"
  value       = aws_lb.main.arn
}

# ARN do Target Group
output "target_group_arn" {
  description = "ARN do Target Group"
  value       = aws_lb_target_group.app.arn
}

# Nome do Target Group
output "target_group_name" {
  description = "Nome do Target Group"
  value       = aws_lb_target_group.app.name
}

# Zone ID do ALB (para Route53)
output "alb_zone_id" {
  description = "Zone ID do ALB para Route53"
  value       = aws_lb.main.zone_id
}
