# =============================================================================
# MÓDULO NETWORKING - RECURSOS DE REDE
# =============================================================================
# Este arquivo cria toda a infraestrutura de rede necessária

# -----------------------------------------------------------------------------
# VPC - Virtual Private Cloud
# -----------------------------------------------------------------------------
# É a "rede privada" na AWS onde todos os recursos vão existir
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true  # Permite nomes DNS internos (ex: ec2-instance.internal)
  enable_dns_support   = true  # Habilita resolução DNS

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-vpc"
    }
  )
}

# -----------------------------------------------------------------------------
# INTERNET GATEWAY
# -----------------------------------------------------------------------------
# É a "porta de entrada/saída" da VPC para a internet
# Sem ele, nada na VPC consegue acessar ou ser acessado pela internet
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-igw"
    }
  )
}

# -----------------------------------------------------------------------------
# SUBNETS PÚBLICAS
# -----------------------------------------------------------------------------
# Subnets com acesso direto à internet (via Internet Gateway)
# Aqui vão recursos que precisam ser acessados publicamente (ex: Load Balancer)
resource "aws_subnet" "public" {
  count = length(var.public_subnet_cidrs)

  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidrs[count.index]
  availability_zone       = var.availability_zones[count.index]
  map_public_ip_on_launch = true  # Recursos aqui ganham IP público automaticamente

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-public-subnet-${count.index + 1}"
      Type = "Public"
    }
  )
}

# -----------------------------------------------------------------------------
# SUBNETS PRIVADAS
# -----------------------------------------------------------------------------
# Subnets SEM acesso direto à internet
# Aqui vão recursos que não devem ser expostos (ex: aplicação, banco de dados)
# Eles podem acessar internet via NAT Gateway (saída apenas)
resource "aws_subnet" "private" {
  count = length(var.private_subnet_cidrs)

  vpc_id            = aws_vpc.main.id
  cidr_block        = var.private_subnet_cidrs[count.index]
  availability_zone = var.availability_zones[count.index]

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-private-subnet-${count.index + 1}"
      Type = "Private"
    }
  )
}

# -----------------------------------------------------------------------------
# NAT GATEWAY - REMOVIDO PARA ECONOMIZAR CUSTOS
# -----------------------------------------------------------------------------
# Em produção, NAT Gateway permite que recursos privados acessem internet
# Custos: ~$32/mês (fora do free tier)
# 
# Para DEV/Free Tier: Removido pois aplicação não precisa acessar internet
# Banco de dados e aplicação funcionam normalmente sem acesso externo
#
# Se precisar no futuro, basta descomentar os blocos aws_eip e aws_nat_gateway

# -----------------------------------------------------------------------------
# ROUTE TABLE - Subnets Públicas
# -----------------------------------------------------------------------------
# Route table = "tabela de rotas" - define por onde o trafego vai
# Para subnets públicas: todo trafego externo (0.0.0.0/0) vai para Internet Gateway
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"  # Qualquer IP externo
    gateway_id = aws_internet_gateway.main.id  # Vai para Internet Gateway
  }

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-public-rt"
    }
  )
}

# Associa a route table com cada subnet pública
resource "aws_route_table_association" "public" {
  count = length(aws_subnet.public)

  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# -----------------------------------------------------------------------------
# ROUTE TABLE - Subnets Privadas
# -----------------------------------------------------------------------------
# Para subnets privadas SEM NAT Gateway: sem rota para internet
# trafego fica restrito apenas à VPC interna
# Isso é OK para aplicação que só se comunica com banco de dados interno
resource "aws_route_table" "private" {
  count = length(var.availability_zones)

  vpc_id = aws_vpc.main.id

  # Sem route para 0.0.0.0/0 = sem acesso à internet
  # Apenas trafego interno da VPC é permitido (rota implícita)

  tags = merge(
    var.tags,
    {
      Name = "${var.project_name}-${var.environment}-private-rt-${count.index + 1}"
    }
  )
}

# Associa cada route table privada com sua subnet privada correspondente
resource "aws_route_table_association" "private" {
  count = length(aws_subnet.private)

  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[count.index].id
}
