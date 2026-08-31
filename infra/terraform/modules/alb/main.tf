# =============================================================================
# MÓDULO ALB - Application Load Balancer
# =============================================================================
# ALB distribui trafego HTTP/HTTPS entre múltiplas instâncias da aplicação
# Faz health checks automáticos e remove instâncias não-saudáveis

# -----------------------------------------------------------------------------
# APPLICATION LOAD BALANCER
# -----------------------------------------------------------------------------
resource "aws_lb" "main" {
  name               = "${var.project_name}-${var.environment}-alb"
  internal           = false  # false = Internet-facing (público)
  load_balancer_type = "application"  # application = Layer 7 (HTTP/HTTPS)
  security_groups    = [var.alb_security_group_id]
  subnets            = var.public_subnet_ids  # Precisa estar em subnets públicas

  # Proteção contra deleção acidental
  enable_deletion_protection = false  # true em produção

  # Access logs (opcional, gera custos no S3)
  # access_logs {
  #   bucket  = aws_s3_bucket.alb_logs.id
  #   enabled = true
  # }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-alb"
    }
  )
}

# -----------------------------------------------------------------------------
# TARGET GROUP
# -----------------------------------------------------------------------------
# Define o grupo de alvos (targets) que receberão trafego
# No nosso caso: ECS tasks (containers)
resource "aws_lb_target_group" "app" {
  name        = "${var.project_name}-${var.environment}-tg"
  port        = var.app_port  # 8080
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"  # ECS Fargate usa IPs dinâmicos

  # Health check - ALB verifica se aplicação está saudável
  health_check {
    enabled             = true
    path                = var.health_check_path  # /actuator/health
    port                = "traffic-port"         # Mesma porta do trafego
    protocol            = "HTTP"
    healthy_threshold   = 2    # 2 checks OK = healthy
    unhealthy_threshold = 5    # 5 checks FAIL = unhealthy (tolerância para app lenta)
    timeout             = 10   # Timeout de 10 segundos (app leva ~60s para iniciar)
    interval            = 30   # Verifica a cada 30 segundos
    matcher             = "200"  # HTTP 200 = OK
  }

  # Deregistration delay - tempo para drenar conexões antes de remover target
  deregistration_delay = 30  # 30 segundos

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-tg"
    }
  )
}

# -----------------------------------------------------------------------------
# LISTENER - HTTP (porta 80)
# -----------------------------------------------------------------------------
# Listener "escuta" trafego em uma porta e encaminha para target group
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  # Ação padrão: encaminhar para target group
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-http-listener"
    }
  )
}

# -----------------------------------------------------------------------------
# LISTENER - HTTPS (porta 443) - OPCIONAL
# -----------------------------------------------------------------------------
# Para HTTPS, você precisaria de um certificado SSL/TLS
# Pode usar AWS Certificate Manager (ACM) - grátis!
#
# Exemplo (descomentado quando tiver certificado):
# resource "aws_lb_listener" "https" {
#   load_balancer_arn = aws_lb.main.arn
#   port              = 443
#   protocol          = "HTTPS"
#   ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
#   certificate_arn   = var.certificate_arn
#
#   default_action {
#     type             = "forward"
#     target_group_arn = aws_lb_target_group.app.arn
#   }
# }
