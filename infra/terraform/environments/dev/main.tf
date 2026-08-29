# =============================================================================
# CONFIGURAÇÃO DO AMBIENTE DEV
# =============================================================================
# Este arquivo ORQUESTRA todos os módulos para criar a infraestrutura completa

terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend para armazenar state do Terraform
  # IMPORTANTE: Descomente isso depois de criar bucket S3 para state
  # backend "s3" {
  #   bucket = "scizor-tracker-terraform-state"
  #   key    = "dev/terraform.tfstate"
  #   region = "us-east-1"
  # }
}

# Provider AWS
provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# -----------------------------------------------------------------------------
# LOCALS - Valores computados
# -----------------------------------------------------------------------------
locals {
  common_tags = {
    Project     = var.project_name
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# -----------------------------------------------------------------------------
# MÓDULO NETWORKING
# -----------------------------------------------------------------------------
module "networking" {
  source = "../../modules/networking"

  project_name         = var.project_name
  environment          = var.environment
  vpc_cidr             = var.vpc_cidr
  availability_zones   = var.availability_zones
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs

  tags = local.common_tags
}

# -----------------------------------------------------------------------------
# MÓDULO SECURITY
# -----------------------------------------------------------------------------
module "security" {
  source = "../../modules/security"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.networking.vpc_id
  vpc_cidr     = module.networking.vpc_cidr

  tags = local.common_tags
}

# -----------------------------------------------------------------------------
# MÓDULO RDS (PostgreSQL)
# -----------------------------------------------------------------------------
module "rds" {
  source = "../../modules/rds"

  project_name            = var.project_name
  environment             = var.environment
  vpc_id                  = module.networking.vpc_id
  private_subnet_ids      = module.networking.private_subnet_ids
  rds_security_group_id   = module.security.rds_security_group_id
  db_name                 = var.db_name
  db_username             = var.db_username
  db_password             = var.db_password
  db_instance_class       = var.db_instance_class
  allocated_storage       = var.allocated_storage
  multi_az                = false  # false para dev/free tier
  skip_final_snapshot     = true   # true para dev
  backup_retention_period = var.backup_retention_period  # 0 para free tier

  tags = local.common_tags
}

# -----------------------------------------------------------------------------
# MÓDULO ALB (Application Load Balancer)
# -----------------------------------------------------------------------------
module "alb" {
  source = "../../modules/alb"

  project_name           = var.project_name
  environment            = var.environment
  vpc_id                 = module.networking.vpc_id
  public_subnet_ids      = module.networking.public_subnet_ids
  alb_security_group_id  = module.security.alb_security_group_id
  app_port               = 8080
  health_check_path      = "/actuator/health"

  tags = local.common_tags
}

# -----------------------------------------------------------------------------
# MÓDULO ECS (Fargate)
# -----------------------------------------------------------------------------
module "ecs" {
  source = "../../modules/ecs"

  project_name                = var.project_name
  environment                 = var.environment
  region                      = var.aws_region
  vpc_id                      = module.networking.vpc_id
  private_subnet_ids          = module.networking.public_subnet_ids  # TEMPORÁRIO: usando subnet pública
  ecs_tasks_security_group_id = module.security.ecs_tasks_security_group_id
  target_group_arn            = module.alb.target_group_arn
  docker_image                = var.docker_image
  cpu                         = 512   # 0.5 vCPU
  memory                      = 1024  # 1GB
  container_port              = 8080
  desired_count               = 1     # 1 task para dev
  enable_autoscaling          = false # Desabilitado para dev
  assign_public_ip            = true  # TEMPORÁRIO: IP público para acessar ECR

  # Variáveis de ambiente da aplicação
  environment_variables = [
    {
      name  = "SPRING_DATASOURCE_URL"
      value = "jdbc:postgresql://${module.rds.db_address}:${module.rds.db_port}/${module.rds.db_name}"
    },
    {
      name  = "SPRING_DATASOURCE_USERNAME"
      value = var.db_username
    },
    {
      name  = "SPRING_DATASOURCE_PASSWORD"
      value = var.db_password  # Senha diretamente (temporário - migrar para VPC Endpoint depois)
    },
    {
      name  = "SERVER_PORT"
      value = "8080"
    }
  ]

  # Senha do banco via Secrets Manager - DESABILITADO (requer VPC Endpoint)
  # Reabilitar após criar VPC Endpoint para secretsmanager
  secrets = []

  tags = local.common_tags

  depends_on = [module.rds]
}

# -----------------------------------------------------------------------------
# SECRETS MANAGER - Senha do Banco
# -----------------------------------------------------------------------------
# Armazena a senha do banco de forma segura
resource "aws_secretsmanager_secret" "db_password" {
  name        = "${var.project_name}-${var.environment}-db-password"
  description = "Senha do PostgreSQL RDS"

  tags = local.common_tags
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}
