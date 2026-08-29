# =============================================================================
# MÓDULO RDS - PostgreSQL Gerenciado
# =============================================================================
# RDS = Relational Database Service
# AWS gerencia: backups, patches, réplicas, monitoramento

# -----------------------------------------------------------------------------
# DB SUBNET GROUP
# -----------------------------------------------------------------------------
# Define em quais subnets o RDS pode ser criado
# Importante: RDS precisa estar em pelo menos 2 AZs diferentes
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.private_subnet_ids  # Subnets PRIVADAS (nunca públicas!)

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-db-subnet-group"
    }
  )
}

# -----------------------------------------------------------------------------
# PARAMETER GROUP
# -----------------------------------------------------------------------------
# Configurações específicas do PostgreSQL
# Aqui você pode ajustar parâmetros como timezone, encoding, etc
resource "aws_db_parameter_group" "main" {
  name   = "${var.project_name}-${var.environment}-postgres-params"
  family = "postgres15"  # Família do PostgreSQL 15

  # Exemplo de parâmetros customizados
  parameter {
    name  = "log_connections"
    value = "1"  # Loga todas as conexões (útil para debug)
  }

  parameter {
    name  = "log_disconnections"
    value = "1"  # Loga desconexões
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-postgres-params"
    }
  )
}

# -----------------------------------------------------------------------------
# RDS INSTANCE - PostgreSQL
# -----------------------------------------------------------------------------
resource "aws_db_instance" "main" {
  # Identificador único da instância
  identifier = "${var.project_name}-${var.environment}-postgres"

  # Engine
  engine         = "postgres"
  engine_version = var.engine_version

  # Classe da instância (tamanho)
  # db.t3.micro = 2 vCPU, 1GB RAM (FREE TIER)
  instance_class = var.db_instance_class

  # Storage
  allocated_storage     = var.allocated_storage  # GB
  storage_type          = "gp2"                  # SSD de propósito geral
  storage_encrypted     = true                   # Criptografia em repouso

  # Database inicial
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password
  port     = 5432

  # Rede
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [var.rds_security_group_id]
  publicly_accessible    = false  # NUNCA público!

  # Parameter group customizado
  parameter_group_name = aws_db_parameter_group.main.name

  # Multi-AZ (alta disponibilidade)
  # false = apenas 1 AZ (economiza custos em dev)
  # true = réplica síncrona em outra AZ (produção)
  multi_az = var.multi_az

  # Backups automáticos
  backup_retention_period = var.backup_retention_period  # Dias (0-35)
  backup_window           = var.backup_window            # Horário UTC
  maintenance_window      = var.maintenance_window       # Horário UTC

  # Snapshot final ao deletar?
  skip_final_snapshot       = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  # Proteção contra deleção acidental (produção)
  deletion_protection = false  # true em produção

  # Auto minor version upgrade (patches de segurança)
  auto_minor_version_upgrade = true

  # Performance Insights (monitoramento avançado)
  # Free tier: 7 dias de retenção
  enabled_cloudwatch_logs_exports = ["postgresql", "upgrade"]

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-postgres"
    }
  )
}
