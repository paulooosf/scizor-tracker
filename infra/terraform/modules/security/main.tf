# =============================================================================
# MÓDULO SECURITY - SECURITY GROUPS (FIREWALLS)
# =============================================================================
# Security Groups funcionam como firewalls virtuais
# Cada recurso terá seu próprio SG com regras específicas

# -----------------------------------------------------------------------------
# SECURITY GROUP - Application Load Balancer (ALB)
# -----------------------------------------------------------------------------
# O ALB é público - recebe trafego da internet
resource "aws_security_group" "alb" {
  name        = "${var.project_name}-${var.environment}-alb-sg"
  description = "Security group para o Application Load Balancer"
  vpc_id      = var.vpc_id

  # INGRESS = trafego ENTRANDO no ALB
  # Regra 1: Permitir HTTP da internet (porta 80)
  ingress {
    description = "HTTP da internet"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]  # 0.0.0.0/0 = qualquer IP da internet
  }

  # Regra 2: Permitir HTTPS da internet (porta 443)
  ingress {
    description = "HTTPS da internet"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # EGRESS = Trafego SAINDO do ALB
  # Permitir todo trafego de saida (ALB precisa se conectar com ECS tasks)
  egress {
    description = "Permitir todo trafego de saida"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"  # -1 = todos os protocolos
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-alb-sg"
    }
  )
}

# -----------------------------------------------------------------------------
# SECURITY GROUP - ECS Tasks (Aplicação)
# -----------------------------------------------------------------------------
# A aplicação roda em containers ECS - deve aceitar apenas trafego do ALB
resource "aws_security_group" "ecs_tasks" {
  name        = "${var.project_name}-${var.environment}-ecs-tasks-sg"
  description = "Security group para ECS tasks (aplicacao Spring Boot)"
  vpc_id      = var.vpc_id

  # INGRESS: Permitir trafego APENAS do ALB na porta 8080
  # Sua aplicação Spring Boot expõe porta 8080
  ingress {
    description     = "Trafego do ALB para aplicacao (porta 8080)"
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]  # Apenas do ALB!
  }

  # EGRESS: Permitir todo trafego de saida
  # Aplicacao precisa se conectar com RDS (PostgreSQL) e eventualmente Kafka
  egress {
    description = "Permitir todo trafego de saida"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-ecs-tasks-sg"
    }
  )
}

# -----------------------------------------------------------------------------
# SECURITY GROUP - RDS PostgreSQL
# -----------------------------------------------------------------------------
# Banco de dados - deve aceitar conexões APENAS da aplicação (ECS tasks)
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "Security group para RDS PostgreSQL"
  vpc_id      = var.vpc_id

  # INGRESS: Permitir PostgreSQL (porta 5432) APENAS de ECS tasks
  ingress {
    description     = "PostgreSQL das ECS tasks"
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_tasks.id]  # Apenas da aplicacao!
  }

  # EGRESS: Permitir todo trafego de saida (RDS precisa fazer updates, backups)
  egress {
    description = "Permitir todo trafego de saida"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-rds-sg"
    }
  )
}

# -----------------------------------------------------------------------------
# SECURITY GROUP - VPC Endpoints (Opcional)
# -----------------------------------------------------------------------------
# Se usar VPC Endpoints para acessar serviços AWS (ECR, S3, etc) sem internet
# Este SG permite trafego HTTPS interno da VPC
resource "aws_security_group" "vpc_endpoints" {
  name        = "${var.project_name}-${var.environment}-vpc-endpoints-sg"
  description = "Security group para VPC Endpoints"
  vpc_id      = var.vpc_id

  # INGRESS: Permitir HTTPS de dentro da VPC
  ingress {
    description = "HTTPS de dentro da VPC"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]  # Apenas de IPs internos da VPC
  }

  # EGRESS: Permitir todo trafego de saida
  egress {
    description = "Permitir todo trafego de saida"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-vpc-endpoints-sg"
    }
  )
}
