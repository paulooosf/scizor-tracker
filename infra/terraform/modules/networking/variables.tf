# =============================================================================
# VARIÁVEIS DO MÓDULO NETWORKING
# =============================================================================
# Este arquivo define os INPUTS que o módulo networking precisa receber

# Nome do projeto - usado para nomear recursos
variable "project_name" {
  description = "Nome do projeto para prefixar recursos"
  type        = string
}

# Ambiente (dev, staging, prod)
variable "environment" {
  description = "Ambiente de deploy (dev, staging, prod)"
  type        = string
}

# CIDR da VPC - Range de IPs disponíveis
# Exemplo: 10.0.0.0/16 = 65.536 IPs disponíveis (10.0.0.0 até 10.0.255.255)
variable "vpc_cidr" {
  description = "CIDR block para a VPC"
  type        = string
  default     = "10.0.0.0/16"
}

# Availability Zones - Zonas de disponibilidade AWS
# São datacenters fisicamente separados para alta disponibilidade
# Se um cair, o outro continua funcionando
variable "availability_zones" {
  description = "Lista de AZs para criar subnets (mínimo 2 para ALB)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

# CIDRs das subnets públicas (uma por AZ)
# Público = tem rota para Internet Gateway
variable "public_subnet_cidrs" {
  description = "Lista de CIDR blocks para subnets públicas"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]  # 256 IPs cada
}

# CIDRs das subnets privadas (uma por AZ)
# Privado = sem rota direta para internet
variable "private_subnet_cidrs" {
  description = "Lista de CIDR blocks para subnets privadas"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]  # 256 IPs cada
}

# Tags comuns para todos os recursos
# Tags ajudam a organizar, filtrar e calcular custos
variable "tags" {
  description = "Tags comuns para todos os recursos"
  type        = map(string)
  default     = {}
}
