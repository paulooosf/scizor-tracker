# =============================================================================
# MÓDULO ECS - Elastic Container Service (Fargate)
# =============================================================================
# ECS Fargate = containers serverless (sem gerenciar EC2)
# AWS gerencia infra, você só se preocupa com o container

# -----------------------------------------------------------------------------
# ECS CLUSTER
# -----------------------------------------------------------------------------
# Cluster = agrupamento lógico de tasks/services
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-${var.environment}-cluster"

  # Container Insights (métricas detalhadas)
  # Free tier: primeiros 10GB de logs grátis
  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-cluster"
    }
  )
}

# -----------------------------------------------------------------------------
# CLOUDWATCH LOG GROUP
# -----------------------------------------------------------------------------
# Logs dos containers vão para CloudWatch
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project_name}-${var.environment}"
  retention_in_days = 7  # Reter logs por 7 dias (ajuste conforme necessário)

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-ecs-logs"
    }
  )
}

# -----------------------------------------------------------------------------
# IAM ROLE - ECS Task Execution
# -----------------------------------------------------------------------------
# Permissões para ECS INICIAR a task (pull image, logs, secrets)
resource "aws_iam_role" "ecs_task_execution" {
  name = "${var.project_name}-${var.environment}-ecs-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = var.tags
}

# Anexar policy padrão da AWS para task execution
resource "aws_iam_role_policy_attachment" "ecs_task_execution" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Policy adicional para acessar Secrets Manager
resource "aws_iam_role_policy" "ecs_secrets_access" {
  name = "${var.project_name}-${var.environment}-secrets-access"
  role = aws_iam_role.ecs_task_execution.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = "*"  # Em prod, especificar ARNs específicos
      }
    ]
  })
}

# -----------------------------------------------------------------------------
# IAM ROLE - ECS Task (Runtime)
# -----------------------------------------------------------------------------
# Permissões que a APLICAÇÃO em execução terá
# Exemplo: acessar S3, SQS, SNS, etc
resource "aws_iam_role" "ecs_task" {
  name = "${var.project_name}-${var.environment}-ecs-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = var.tags
}

# Adicione policies conforme necessário
# Exemplo: acesso ao S3
# resource "aws_iam_role_policy_attachment" "ecs_task_s3" {
#   role       = aws_iam_role.ecs_task.name
#   policy_arn = "arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess"
# }

# -----------------------------------------------------------------------------
# TASK DEFINITION
# -----------------------------------------------------------------------------
# "Receita" do container: imagem, CPU, RAM, portas, variáveis, etc
resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project_name}-${var.environment}-app"
  network_mode             = "awsvpc"  # Cada task tem seu próprio ENI (IP)
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.cpu
  memory                   = var.memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "${var.project_name}-app"
      image     = var.docker_image
      essential = true

      portMappings = [
        {
          containerPort = var.container_port
          hostPort      = var.container_port
          protocol      = "tcp"
        }
      ]

      # Variáveis de ambiente
      environment = var.environment_variables

      # Secrets do AWS Secrets Manager
      secrets = var.secrets

      # Logs
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "ecs"
        }
      }

      # Health check do container (opcional, além do ALB)
      healthCheck = {
        command     = ["CMD-SHELL", "curl -f http://localhost:${var.container_port}/actuator/health || exit 1"]
        interval    = 30
        timeout     = 10
        retries     = 5
        startPeriod = 120  # Aguarda 120s antes do primeiro check (app leva ~60s + margem)
      }
    }
  ])

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-task-definition"
    }
  )
}

# -----------------------------------------------------------------------------
# ECS SERVICE
# -----------------------------------------------------------------------------
# Service mantém N tasks rodando, gerencia rolling updates, integra com ALB
resource "aws_ecs_service" "app" {
  name            = "${var.project_name}-${var.environment}-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  # Grace period para health checks (tempo para app iniciar antes de ser considerada unhealthy)
  health_check_grace_period_seconds = 180  # 3 minutos (app leva ~60s + margem)

  # Estratégia de deployment
  deployment_minimum_healthy_percent = 50   # Mínimo 50% de tasks saudáveis
  deployment_maximum_percent         = 200  # Máximo 200% durante deploy (pode ter 2x tasks)

  # Rede
  network_configuration {
    subnets          = var.private_subnet_ids
    security_groups  = [var.ecs_tasks_security_group_id]
    assign_public_ip = var.assign_public_ip
  }

  # Integração com ALB
  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = "${var.project_name}-app"
    container_port   = var.container_port
  }

  # Service discovery (opcional)
  # service_registries { ... }

  # Circuit breaker (rollback automático se deploy falhar)
  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  # Aguardar ALB estar pronto antes de criar service
  depends_on = [aws_iam_role_policy_attachment.ecs_task_execution]

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-service"
    }
  )
}

# -----------------------------------------------------------------------------
# AUTO SCALING (Opcional)
# -----------------------------------------------------------------------------
# Escala automaticamente baseado em métricas (CPU, memória, requests)
resource "aws_appautoscaling_target" "ecs" {
  count = var.enable_autoscaling ? 1 : 0

  max_capacity       = var.max_capacity
  min_capacity       = var.min_capacity
  resource_id        = "service/${aws_ecs_cluster.main.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  service_namespace  = "ecs"
}

# Policy de auto scaling baseada em CPU
resource "aws_appautoscaling_policy" "ecs_cpu" {
  count = var.enable_autoscaling ? 1 : 0

  name               = "${var.project_name}-${var.environment}-cpu-autoscaling"
  policy_type        = "TargetTrackingScaling"
  resource_id        = aws_appautoscaling_target.ecs[0].resource_id
  scalable_dimension = aws_appautoscaling_target.ecs[0].scalable_dimension
  service_namespace  = aws_appautoscaling_target.ecs[0].service_namespace

  target_tracking_scaling_policy_configuration {
    target_value       = 70.0  # Manter CPU em ~70%
    scale_in_cooldown  = 300   # Aguardar 5min antes de reduzir
    scale_out_cooldown = 60    # Aguardar 1min antes de aumentar

    predefined_metric_specification {
      predefined_metric_type = "ECSServiceAverageCPUUtilization"
    }
  }
}
