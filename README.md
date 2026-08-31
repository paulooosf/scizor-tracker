<div align="center">

<h1>Scizor Tracker</h1>

<h3>Sistema de rastreamento e gerenciamento de bugs.</h3>

<img alt="Java" src="https://img.shields.io/badge/JAVA-%23f89820?style=for-the-badge&logo=openjdk&logoColor=f89820&labelColor=%23000000">
<img alt="Spring" src="https://img.shields.io/badge/SPRING-%236DB33F?style=for-the-badge&logo=spring&logoColor=%236DB33F&labelColor=black">
<img alt="PostgreSQL" src="https://img.shields.io/badge/postgresql-%234169E1?style=for-the-badge&logo=postgresql&logoColor=%234169E1&labelColor=black">
<img alt="Docker" src="https://img.shields.io/badge/docker-%232496ED?style=for-the-badge&logo=docker&logoColor=%232496ED&labelColor=black">
<img alt="Apache Kafka" src="https://img.shields.io/badge/kafka-%23231F20?style=for-the-badge&logo=apachekafka&labelColor=000000">
<img alt="JWT" src="https://img.shields.io/badge/jwt-000000?style=for-the-badge&logo=jsonwebtokens&labelColor=000000">
<img alt="AWS" src="https://img.shields.io/badge/AWS-%23FF9900?style=for-the-badge&logo=amazonwebservices&logoColor=%23FF9900&labelColor=black">
<img alt="Github Actions" src="https://img.shields.io/badge/github_actions-%232088FF?style=for-the-badge&logo=githubactions&logoColor=%232088FF&labelColor=black">
<img alt="Prometheus" src="https://img.shields.io/badge/prometheus-%23E6522C?style=for-the-badge&logo=prometheus&logoColor=%23E6522C&labelColor=black">
<img alt="Grafana" src="https://img.shields.io/badge/grafana-%23F46800?style=for-the-badge&logo=grafana&logoColor=%23F46800&labelColor=black">
<img alt="Terraform" src="https://img.shields.io/badge/terraform-%237B42BC?style=for-the-badge&logo=terraform&logoColor=%237B42BC&labelColor=black">

</div>

<p align="center">
  <a href="#funcionalidades">Funcionalidades</a> •
  <a href="#arquitetura">Arquitetura</a> •
  <a href="#documentacao">Documentação</a> •
  <a href="#como-rodar">Como rodar</a> •
  <a href="#fluxo-de-autenticacao">Autenticação</a> •
  <a href="#deploy-aws">Deploy AWS</a> •
  <a href="#observabilidade-e-debug">Observabilidade</a> •
  <a href="#futuras-melhorias">Futuras melhorias</a> •
  <a href="#créditos">Créditos</a>
</p>

---

## Funcionalidades

### Visão Geral

O **Scizor Tracker** é um sistema completo de rastreamento de bugs construído com **Spring Boot 4.1.1**, integrando as melhores práticas de desenvolvimento moderno:

- **API RESTful** com autenticação JWT e controle de acesso baseado em roles (USER/ADMIN)
- **Persistência relacional** com PostgreSQL + JPA/Hibernate
- **Mensageria assíncrona** com Apache Kafka (branch `main` - desenvolvimento local)
- **Testes robustos**: Unitários (Mockito/JUnit 5) + Integração (Testcontainers) com **70%+ de cobertura** via JaCoCo
- **Observabilidade nativa**: Spring Boot Actuator + Prometheus + Grafana
- **Deploy automatizado**: CI/CD com GitHub Actions → AWS ECS Fargate (branch `prod`)
- **Infraestrutura como código**: Terraform gerenciando 33 recursos AWS (VPC, ECS, RDS, ALB, ECR, CloudWatch)
- **Documentação OpenAPI 3.0**: Swagger UI interativo com exemplos completos

> **Nota sobre branches:**
> - **`main`**: Ambiente de desenvolvimento com Kafka, Prometheus, Grafana (docker-compose completo)
> - **`prod`**: AWS-ready sem Kafka, otimizado para ECS Fargate com custos reduzidos

---

### Gestão de Bugs

- Endpoints: listar (paginado), buscar por ID, criar, atualizar, deletar, atualizar status, atribuir/remover responsável
- CRUD completo de bugs com validações de negócio e regras de transição de status
- Status: `ABERTO` → `EM_ANDAMENTO` → `RESOLVIDO` → `FECHADO` (ou `REABERTO`)
- Prioridades: `BAIXA`, `MEDIA`, `ALTA`, `CRITICA`
- Busca avançada: por projeto, status, prioridade, responsável, termo livre, sem responsável
- Atribuição/remoção de responsáveis com validação de existência
- Paginação completa em todos os endpoints de listagem
- Base de dados: PostgreSQL
- Porta: 8080

### Gestão de Projetos

- Endpoints: listar (paginado), buscar por ID, buscar por nome, criar, atualizar, deletar
- CRUD completo de projetos de software
- Associação de bugs a projetos (relacionamento One-to-Many)
- Busca textual parcial e case-insensitive no nome do projeto
- Deleção em cascata (ao deletar projeto, todos os bugs são removidos)
- Base de dados: PostgreSQL
- Porta: 8080

### Sistema de Comentários

- Endpoints: listar (paginado), buscar por ID, buscar por bug, buscar por usuário, criar, atualizar, deletar
- Adicionar comentários aos bugs com rastreamento de autor e timestamp
- Busca de comentários por bug (ordenação cronológica) e por usuário
- Suporte a texto formatado (markdown-ready)
- Atualização de texto e deleção permanente
- Base de dados: PostgreSQL
- Porta: 8080

### Gestão de Usuários

- Endpoints: listar (paginado), buscar por ID, buscar por email, criar (público), atualizar, deletar
- Cadastro público de usuários (não requer autenticação)
- Senhas criptografadas com **BCrypt** (12 rounds)
- Email único (validação de duplicidade)
- Controle de acesso com roles: `USER` e `ADMIN`
- Ao deletar usuário, bugs atribuídos têm responsável removido automaticamente
- Base de dados: PostgreSQL
- Porta: 8080

### Autenticação e Autorização

- Endpoints públicos: `/api/autenticar/login`, `/api/autenticar/esqueci-senha`, `/api/autenticar/redefinir-senha`
- **JWT (JSON Web Token)** com Auth0 java-jwt library
- Token expira em 2 horas (configurável via `api.security.token.secret`)
- Endpoints protegidos por role (`@PreAuthorize("hasRole('ADMIN')")`)
- Sistema de recuperação de senha com token temporário (validade 15 minutos)
- Credenciais de teste: `admin@scizor.com/admin123` (ADMIN) ou `joao.silva@example.com/senha123` (USER)
- Porta: 8080

### Mensageria (Kafka)

> **Nota sobre branches:**
> - **Branch `main`**: Contém integração completa com Kafka (desenvolvimento local)
> - **Branch `prod`**: AWS-ready **sem Kafka** (deploy ECS Fargate)

- Tópicos: `bug.criado`, `bug.status.alterado`, `bug.responsavel.atribuido`, `comentario.adicionado`, `bug.critico`
- Publicação de eventos assíncronos após persistência no banco
- Kafka UI para monitoramento de tópicos e consumidores
- Porta Kafka: 9092 | Porta Kafka UI: 8090

### Observabilidade

- **Spring Boot Actuator**: Health checks (`/actuator/health`) com detalhes de banco e SSL
- **Prometheus Metrics**: Endpoint `/actuator/prometheus` com métricas de HTTP, JVM e custom tags
- **Grafana Dashboards**: Painéis de monitoramento com datasource Prometheus pré-configurado
- **CloudWatch Logs**: Logs centralizados na AWS (branch `prod`)
- Portas: Prometheus 9090 | Grafana 3000

### CI/CD (GitHub Actions)

- Pipeline automatizado: Build Maven → Testes → Docker Build → Push ECR → Deploy ECS
- Trigger: push na branch `prod` ou execução manual
- Rollback automático em caso de falha no health check
- Limpeza de imagens antigas no ECR (mantém últimas 5)
- Documentação completa em `.github/SETUP_CICD.md` e `CICD.md`

---

## Documentação

Todos os endpoints estão documentados via **Swagger/OpenAPI 3.0** com descrições detalhadas, exemplos e códigos de resposta. Após iniciar o projeto, acesse:

| Ambiente | URL |
|---------|-----|
| **Local (Docker)** | http://localhost:8080/swagger-ui.html |
| **AWS (branch prod)** | http://<ALB_DNS>/swagger-ui.html |
| **Health Check Local** | http://localhost:8080/actuator/health |
| **Prometheus Metrics Local** | http://localhost:8080/actuator/prometheus |
| **Kafka UI (branch main)** | http://localhost:8090 (user: não requer / password: não requer) |
| **Grafana** | http://localhost:3000 (user: `admin` / password: `admin`) |
| **Prometheus** | http://localhost:9090 |

A coleção **Postman** está disponível em `Scizor-Tracker.postman_collection.json` para testes rápidos dos endpoints com variáveis de ambiente pré-configuradas.

---

## Arquitetura

A arquitetura segue o padrão **RESTful** com autenticação JWT, persistência relacional e observabilidade nativa:

- **Spring Boot Application** (Porta 8080): API REST com autenticação JWT, CRUD de bugs/projetos/comentários/usuários
- **PostgreSQL** (Porta 5432): Banco relacional com JPA/Hibernate (DDL auto-update + data.sql para seed)
- **Apache Kafka** (Porta 9092, branch `main`): Event-driven architecture para desacoplamento de serviços
- **Kafka UI** (Porta 8090, branch `main`): Interface web para monitoramento de tópicos e mensagens
- **Prometheus** (Porta 9090, branch `main`): Coleta de métricas da aplicação via Actuator
- **Grafana** (Porta 3000, branch `main`): Dashboards de observabilidade com datasource Prometheus
- **AWS ECS Fargate** (branch `prod`): Execução de containers sem gerenciar servidores
- **AWS RDS PostgreSQL** (branch `prod`): Banco gerenciado com Multi-AZ
- **AWS ECR** (branch `prod`): Repositório privado de imagens Docker
- **Application Load Balancer** (branch `prod`): Balanceamento de carga com health checks automáticos

### Fluxo de Autenticação (JWT)

```mermaid
sequenceDiagram
    participant Client as Cliente/API
    participant Auth as AuthController
    participant Spring as Spring Security
    participant Token as TokenService
    participant DB as PostgreSQL

    Client->>Auth: POST /api/autenticar/login<br/>{email, senha}
    Auth->>Spring: authenticate(email, senha)
    Spring->>DB: Buscar usuário por email
    DB-->>Spring: Usuário encontrado
    Spring->>Spring: Validar senha (BCrypt)
    alt Credenciais válidas
        Spring-->>Auth: Authentication com Principal
        Auth->>Token: gerarToken(usuario)
        Token->>Token: JWT.create() + sign(secret)
        Token-->>Auth: Token JWT (validade 2h)
        Auth-->>Client: 200 OK {token: "eyJhbGc..."}
    else Credenciais inválidas
        Spring-->>Auth: AuthenticationException
        Auth-->>Client: 401 Unauthorized
    end

    Client->>Auth: GET /api/bugs<br/>Authorization: Bearer <token>
    Auth->>Spring: SecurityFilterChain
    Spring->>Token: validarToken(token)
    Token->>Token: JWT.verify(token)
    alt Token válido
        Token-->>Spring: Email (subject)
        Spring->>DB: Buscar usuário por email
        DB-->>Spring: Usuário com role
        Spring->>Spring: Verificar permissão (@PreAuthorize)
        Spring-->>Auth: Acesso autorizado
        Auth-->>Client: 200 OK + dados
    else Token inválido/expirado
        Token-->>Spring: JWTVerificationException
        Spring-->>Client: 403 Forbidden
    end
```

### Arquitetura AWS (Branch prod)

```mermaid
graph TB
    subgraph Internet
        USER[👤 Usuário/Cliente]
        GHA[⚙️ GitHub Actions<br/>CI/CD Pipeline]
    end
    
    subgraph AWS["☁️ AWS Cloud (us-east-1)"]
        subgraph VPC["🔒 VPC: 10.0.0.0/16"]
            subgraph PublicSubnets["🌐 Subnets Públicas (2 AZs)"]
                ALB[⚖️ Application Load Balancer<br/>Health Check: /actuator/health<br/>Target: ECS Tasks:8080]
            end
            
            subgraph PublicSubnetsECS["🌐 Subnets Públicas para ECS Tasks"]
                ECS1[🐳 ECS Task 1<br/>0.5 vCPU + 1GB RAM<br/>Spring Boot App]
                ECS2[🐳 ECS Task 2<br/>Standby/Auto-scaling]
            end
            
            subgraph PrivateSubnets["🔐 Subnets Privadas (2 AZs)"]
                RDS[(🗄️ RDS PostgreSQL<br/>db.t3.micro<br/>Multi-AZ disabled)]
            end
            
            subgraph SecurityGroups["🛡️ Security Groups"]
                SG_ALB[SG ALB: Ingress 80/443]
                SG_ECS[SG ECS: Ingress 8080 do ALB]
                SG_RDS[SG RDS: Ingress 5432 do ECS]
            end
        end
        
        ECR[📦 Amazon ECR<br/>scizor-tracker:latest<br/>scizor-tracker:sha]
        CW[📊 CloudWatch Logs<br/>/ecs/scizor-tracker-dev<br/>Retention: 7 dias]
        IAM[🔑 IAM Roles<br/>ecsTaskExecutionRole<br/>ecsTaskRole]
    end
    
    USER -->|HTTP/HTTPS| ALB
    ALB -->|Port 8080| ECS1
    ALB -.->|Scaling| ECS2
    ECS1 -->|JDBC 5432| RDS
    ECS2 -.->|JDBC 5432| RDS
    GHA -->|Push Image| ECR
    ECR -.->|Pull Image| ECS1
    ECR -.->|Pull Image| ECS2
    ECS1 -->|Logs| CW
    ECS2 -.->|Logs| CW
    ECS1 -.->|Assume Role| IAM
    ECS2 -.->|Assume Role| IAM
    
    SG_ALB -.->|Protege| ALB
    SG_ECS -.->|Protege| ECS1
    SG_ECS -.->|Protege| ECS2
    SG_RDS -.->|Protege| RDS
    
    style USER fill:#4A90E2
    style GHA fill:#2088FF
    style ALB fill:#FF9900
    style ECS1 fill:#FF9900
    style ECS2 fill:#FF9900
    style RDS fill:#527FFF
    style ECR fill:#FF9900
    style CW fill:#FF9900
    style IAM fill:#DD344C
    style SecurityGroups fill:#232F3E
```

**Características de Resiliência:**
- **Alta Disponibilidade**: Multi-AZ (2 zonas de disponibilidade)
- **Auto Scaling**: Circuit breaker com desiredCount ajustável
- **Zero Downtime**: Rolling updates com health check antes de finalizar deploy
- **Rollback Automático**: Pipeline GitHub Actions reverte deploy em caso de falha
- **Security Groups**: Least privilege com regras específicas por camada
- **CloudWatch Monitoring**: Métricas de CPU, RAM, network e logs centralizados

### Fluxo de Exemplo: Criar Bug e Publicar Evento (Branch main com Kafka)

```mermaid
sequenceDiagram
    participant Client as Cliente/API
    participant Auth as SecurityFilter
    participant Controller as BugController
    participant Service as BugService
    participant Repo as BugRepository
    participant DB as PostgreSQL
    participant Kafka as Apache Kafka
    participant Topic as Tópico bug.criado

    Client->>Auth: POST /api/bugs?projetoId=1<br/>Authorization: Bearer <token>
    Auth->>Auth: Validar JWT
    Auth->>Auth: Verificar role (ADMIN)
    Auth-->>Controller: Acesso autorizado
    
    Controller->>Service: criar(bugDto, projetoId)
    Service->>Repo: findById(projetoId)
    Repo->>DB: SELECT * FROM projetos WHERE id=1
    DB-->>Repo: Projeto encontrado
    Repo-->>Service: Projeto
    
    Service->>Service: Validar dados (Bean Validation)
    Service->>Repo: save(bug)
    Repo->>DB: INSERT INTO bugs (...)
    DB-->>Repo: Bug criado com ID
    Repo-->>Service: Bug persistido
    
    Service->>Kafka: publish(BugCriadoEvento)
    Kafka->>Topic: Gravar mensagem
    Topic-->>Kafka: ACK
    Kafka-->>Service: Evento publicado
    
    Service-->>Controller: Bug criado
    Controller-->>Client: 201 Created {bugDto}
    
    Note over Topic: Consumidores processam<br/>evento de forma assíncrona
```

---

## Como rodar

### Pré-requisitos

- [Docker](https://docs.docker.com/get-started/get-docker/) e Docker Compose (v2.0+)
- Espaço em disco: ~2GB para imagens Docker

### Passos

1. Clone o repositório:

```bash
git clone https://github.com/paulooosf/scizor-tracker.git
cd scizor-tracker
```

2. **(Branch main)** Faça build e inicie todos os serviços (PostgreSQL + Kafka + Zookeeper + App + Kafka UI + Prometheus + Grafana):

```bash
docker compose up --build
```

> Para branch `prod` (sem Kafka), use: `docker compose -f docker-compose-prod.yml up --build` (se existir, ou remova serviços Kafka manualmente)

3. Aguarde a inicialização (~40-60 segundos). Em outro terminal, verifique o status:

```bash
docker compose ps
```

Todos os serviços devem estar com status `Up` ou `healthy`.

4. Acesse os dashboards:

   - **Swagger API Docs**: http://localhost:8080/swagger-ui.html
   - **Health Check**: http://localhost:8080/actuator/health
   - **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
   - **Kafka UI** (branch main): http://localhost:8090
   - **Prometheus** (branch main): http://localhost:9090
   - **Grafana** (branch main): http://localhost:3000 (admin/admin)

5. **Credenciais de teste** (criadas automaticamente via `data.sql`):

```
ADMIN: admin@scizor.com / admin123
USER:  joao.silva@example.com / senha123
```

6. **Teste rápido**:

   - **Via navegador**: Acesse http://localhost:8080/swagger-ui.html e teste os endpoints interativamente
   - **Via Postman**: Importe a coleção `Scizor-Tracker.postman_collection.json` da raiz do projeto
     - Collection já vem com variáveis de ambiente pré-configuradas
     - Endpoints organizados por módulo (Autenticação, Bugs, Projetos, Comentários, Usuários)
     - Exemplos de requisições para todos os casos de uso

7. Para parar os serviços:

```bash
docker compose down
```

Para limpar volumes (apaga dados do banco):

```bash
docker compose down -v
```

---

## Fluxo de Mensageria (Branch main)

A comunicação assíncrona é feita via **Apache Kafka** com padrão event-driven:

### Tópicos Kafka

```mermaid
graph TB
    subgraph Producers["📤 Publicadores (BugService)"]
        P1[BugService.criar]
        P2[BugService.atualizarStatus]
        P3[BugService.atribuirResponsavel]
        P4[ComentarioService.criar]
    end
    
    subgraph Topics["📨 Tópicos Kafka"]
        T1[bug.criado]
        T2[bug.status.alterado]
        T3[bug.responsavel.atribuido]
        T4[comentario.adicionado]
        T5[bug.critico]
    end
    
    subgraph Consumers["📥 Consumidores (Futuros)"]
        C1[NotificacaoConsumidor<br/>Enviar email via SES]
        C2[AuditoriaConsumidor<br/>Log de mudanças]
        C3[IntegracaoConsumidor<br/>Webhook externo]
    end
    
    P1 -->|Publica| T1
    P2 -->|Publica| T2
    P2 -->|Se prioridade=CRITICA| T5
    P3 -->|Publica| T3
    P4 -->|Publica| T4
    
    T1 -.->|Consome| C1
    T2 -.->|Consome| C1
    T3 -.->|Consome| C1
    T4 -.->|Consome| C2
    T5 -.->|Consome| C3
    
    style Producers fill:#6DB33F
    style Topics fill:#231F20
    style Consumers fill:#FF6600
```

### Exemplo de Payload: BugCriadoEvento

```json
{
  "bugId": 1,
  "titulo": "Erro ao processar pagamento",
  "descricao": "Sistema retorna 500 ao tentar processar pagamento com cartão",
  "prioridade": "ALTA",
  "status": "ABERTO",
  "projetoId": 1,
  "projetoNome": "Sistema de Pagamentos",
  "dataCriacao": "2026-08-27T14:30:00Z"
}
```

### Características de Resiliência (Kafka)

- **Partições**: 1 partição por tópico (ajustável para maior throughput)
- **Replication Factor**: 1 (desenvolvimento local, aumentar para produção)
- **Auto Create Topics**: Habilitado (tópicos criados automaticamente no primeiro publish)
- **Kafka UI**: Monitoramento visual de mensagens, offsets e lag dos consumidores
- **Dead Letter Queue**: Não implementado ainda (futuro: retry policy + DLQ)

> **Branch prod (AWS):** Kafka foi removido para reduzir custos. Eventos serão implementados via **AWS EventBridge** ou **SNS/SQS** em versões futuras.

---

## Deploy AWS (Branch prod)

### Infraestrutura como Código (Terraform)

A infraestrutura completa está em `infra/terraform/` e cria **33 recursos AWS**:

| Recurso | Descrição | Quantidade | Custo Mensal* |
|---------|-----------|------------|---------------|
| **VPC** | Rede isolada 10.0.0.0/16 | 1 | Grátis |
| **Subnets** | 2 públicas + 2 privadas (Multi-AZ) | 4 | Grátis |
| **Internet Gateway** | Conectividade pública | 1 | Grátis |
| **Route Tables** | Roteamento público e privado | 2 | Grátis |
| **Security Groups** | ALB, ECS, RDS | 3 | Grátis |
| **ECS Fargate** | Cluster + Service + Task Definition | 3 | ~$15 |
| **Application Load Balancer** | ALB + Target Group + Listeners | 3 | ~$16 |
| **RDS PostgreSQL** | db.t3.micro (1 vCPU + 1GB RAM) | 1 | ~$15 |
| **ECR Repository** | Repositório privado de imagens | 1 | ~$0.50 |
| **CloudWatch Log Group** | Logs da aplicação (retenção 7 dias) | 1 | ~$0.50 |
| **IAM Roles** | ecsTaskExecutionRole + ecsTaskRole | 2 | Grátis |
| **Total** | | **33** | **~$47/mês** |

*Após expirar o free tier da AWS. Considera 1 task ECS 24/7, RDS sem Multi-AZ, 1GB logs/mês.

### Pré-requisitos Terraform

- AWS CLI configurado (`aws configure`)
- Terraform instalado (v1.5+)
- Credenciais AWS com permissões: VPC, ECS, RDS, ECR, ALB, IAM, CloudWatch

### Deploy da Infraestrutura

1. Configure variáveis de ambiente:

```bash
cd infra/terraform/environments/dev
```

2. Crie arquivo `terraform.tfvars` com suas credenciais:

```hcl
db_username = "scizor_admin"
db_password = "SuaSenhaSegura123!"
jwt_secret  = "seu-secret-jwt-production-2026"
```

3. Inicialize Terraform:

```bash
terraform init
```

4. Revise o plano de execução:

```bash
terraform plan
```

5. Aplique (cria ~33 recursos, leva ~8-10 minutos):

```bash
terraform apply -auto-approve
```

6. Ao final, você verá os outputs:

```
Outputs:

alb_url = "http://scizor-tracker-dev-alb-1487669364.us-east-1.elb.amazonaws.com"
ecr_repository_url = "451834067280.dkr.ecr.us-east-1.amazonaws.com/scizor-tracker"
ecs_cluster_name = "scizor-tracker-dev-cluster"
ecs_service_name = "scizor-tracker-dev-service"
rds_endpoint = "scizor-tracker-dev-postgres.c8bqky6qixvp.us-east-1.rds.amazonaws.com:5432"
```

7. Verifique o health check:

```bash
curl http://<ALB_URL>/actuator/health
# Esperado: {"status":"UP","components":{"db":{"status":"UP"}}}
```

### Destruir Infraestrutura

```bash
terraform destroy -auto-approve
```

> **Atenção:** Isso removerá **permanentemente** todos os recursos, incluindo banco de dados. Faça backup antes!

### CI/CD com GitHub Actions (Branch prod)

Pipeline automatizado que faz deploy na AWS a cada push na branch `prod`:

**Fluxo Completo:**

```mermaid
graph LR
    A[📝 Push branch prod] --> B[🔨 Build Maven]
    B --> C[✅ Testes Unitários]
    C --> D[🐳 Docker Build]
    D --> E[📦 Push para ECR]
    E --> F[🚀 Update ECS Task]
    F --> G[⏳ Health Check 10min]
    G --> H{Healthy?}
    H -->|Sim| I[✅ Deploy Completo]
    H -->|Não| J[❌ Rollback Automático]
    I --> K[🧹 Limpeza ECR]
```

**Setup CI/CD (5 minutos):**

1. Siga o guia detalhado em `.github/SETUP_CICD.md`

2. Configure 6 secrets no GitHub (`Settings > Secrets and variables > Actions`):

```
AWS_ACCESS_KEY_ID         → AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY     → wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_REGION                → us-east-1
ECR_REPOSITORY            → scizor-tracker
ECS_CLUSTER               → scizor-tracker-dev-cluster
ECS_SERVICE               → scizor-tracker-dev-service
```

3. Faça push na branch `prod` para triggerar o pipeline:

```bash
git checkout prod
git add .
git commit -m "feat: trigger CI/CD pipeline"
git push origin prod
```

4. Acompanhe o progresso em `Actions` no GitHub

**Documentação completa:** Veja `CICD.md` para troubleshooting, rollback manual e monitoramento.

---

## Observabilidade e Debug

### Dashboards Disponíveis

1. **Swagger/OpenAPI**
   - URL Local: http://localhost:8080/swagger-ui.html
   - URL AWS: http://<ALB_DNS>/swagger-ui.html
   - Use para: Testar endpoints em tempo real com autenticação JWT

2. **Spring Boot Actuator**
   - URL: http://localhost:8080/actuator/health
   - Use para: Verificar status da aplicação, banco, SSL e liveness/readiness probes

3. **Prometheus Metrics**
   - URL: http://localhost:8080/actuator/prometheus
   - Use para: Integrar com Grafana ou coletar métricas customizadas

4. **Grafana Dashboards** (branch main)
   - URL: http://localhost:3000
   - Credenciais: `admin` / `admin`
   - Use para: Visualizar dashboards de HTTP requests, JVM memory, CPU, throughput

5. **Kafka UI** (branch main)
   - URL: http://localhost:8090
   - Use para: Monitorar tópicos, mensagens, offsets, lag de consumidores

6. **CloudWatch Logs** (branch prod - AWS)
   - Console AWS: CloudWatch → Log Groups → `/ecs/scizor-tracker-dev`
   - CLI: `aws logs tail /ecs/scizor-tracker-dev --region us-east-1 --follow`
   - Use para: Debug de erros em produção, auditoria de requisições

### Verificação de Saúde

**Local (navegador ou Postman):**
- Health check: http://localhost:8080/actuator/health
- Métricas Prometheus: http://localhost:8080/actuator/prometheus
- Swagger UI: http://localhost:8080/swagger-ui.html

**AWS (branch prod - navegador):**
- Health check: http://<ALB_URL>/actuator/health
- Swagger UI: http://<ALB_URL>/swagger-ui.html

**AWS CLI (linha de comando):**

```bash
# Ver logs ECS em tempo real
aws logs tail /ecs/scizor-tracker-dev --region us-east-1 --since 10m --follow

# Verificar status do serviço ECS
aws ecs describe-services \
  --cluster scizor-tracker-dev-cluster \
  --services scizor-tracker-dev-service \
  --query 'services[0].[status,runningCount,desiredCount]' \
  --region us-east-1
```

### Troubleshooting

**Problema: Aplicação não inicia (Docker local)**

```bash
# Ver logs da aplicação
docker compose logs scizor-tracker-app -f

# Verificar se PostgreSQL está rodando
docker compose ps postgres

# Reiniciar apenas a aplicação
docker compose restart scizor-tracker-app
```

**Problema: Erro 401 Unauthorized ao chamar endpoint protegido**

- Verifique se o token JWT está no header: `Authorization: Bearer <token>`
- Token pode ter expirado (validade: 2 horas)
- Faça login novamente em `/api/autenticar/login` para obter novo token
- Confirme que o usuário tem a role adequada (USER para GET, ADMIN para POST/PUT/DELETE)

**Problema: Erro 403 Forbidden (token válido mas sem permissão)**

- Endpoint requer role ADMIN, mas usuário logado tem role USER
- Verifique a role do usuário em `/api/usuarios/{id}`
- Faça login com conta ADMIN: `admin@scizor.com / admin123`

**Problema: Deploy ECS falha (AWS - branch prod)**

```bash
# Verificar eventos do serviço (últimos 5)
aws ecs describe-services \
  --cluster scizor-tracker-dev-cluster \
  --services scizor-tracker-dev-service \
  --query 'services[0].events[0:5]' \
  --region us-east-1

# Ver logs da task mais recente
aws logs tail /ecs/scizor-tracker-dev --region us-east-1 --since 30m

# Verificar se a task está rodando
aws ecs list-tasks \
  --cluster scizor-tracker-dev-cluster \
  --service-name scizor-tracker-dev-service \
  --region us-east-1
```

**Problema: RDS Connection Timeout (AWS - branch prod)**

- Verifique security groups: RDS deve aceitar conexões do security group do ECS
- Confirme que tasks estão nas subnets públicas (temporário para free tier)
- Verifique endpoint RDS em `terraform output rds_endpoint`
- Teste conectividade: `aws ecs execute-command` (se habilitado no task definition)

**Problema: Mensagens não são consumidas do Kafka (branch main)**

```bash
# Acessar Kafka UI
open http://localhost:8090

# Verificar se tópico existe e tem mensagens
# Verificar se consumer group está ativo

# Logs do consumidor (se implementado)
docker compose logs scizor-tracker-app | grep "Consumindo evento"
```

**Problema: Grafana não exibe métricas (branch main)**

```bash
# Verificar se Prometheus está coletando métricas
curl http://localhost:9090/api/v1/targets | jq

# Verificar se datasource está configurado no Grafana
# Grafana → Configuration → Data Sources → Prometheus (http://prometheus:9090)

# Reiniciar Grafana
docker compose restart grafana
```

---

## Stack Técnico

| Componente | Tecnologia | Versão |
|-----------|-----------|--------|
| Linguagem | Java | 17+ |
| Framework Web | Spring Boot | 3.4.1 |
| Persistência | Spring Data JPA | 3.4.1 |
| Segurança | Spring Security + JWT | 6.4.2 |
| JWT Library | Auth0 java-jwt | 4.4.0 |
| Documentação API | Springdoc OpenAPI | 2.7.0 |
| Banco Relacional | PostgreSQL | 15+ |
| Message Broker (main) | Apache Kafka | 7.5.0 (Confluent) |
| Kafka UI (main) | Provectus Kafka UI | latest |
| Monitoramento | Micrometer + Prometheus | 1.14.2 |
| Dashboards (main) | Grafana | 10.2.2 |
| Build | Maven | 3.9+ |
| Containerização | Docker | 24.0+ |
| Orquestração Local | Docker Compose | 2.0+ |
| Infraestrutura | Terraform | 1.5+ |
| CI/CD | GitHub Actions | - |
| Cloud Provider (prod) | AWS | ECS, RDS, ECR, ALB, CloudWatch |
| **Testes** | **JUnit 5 + Mockito** | **5.10+** |
| **Testes Integração** | **Testcontainers + PostgreSQL** | **1.19+** |
| **Cobertura** | **JaCoCo (70%+ obrigatório)** | **0.8.11** |

---

## Futuras melhorias

Conforme o intuito do projeto, com o tempo, novas funcionalidades serão adicionadas a fim de aplicar novos estudos, como:

### Infraestrutura e Cloud

- Migrar tasks ECS para subnets privadas + NAT Gateway (segurança)
- Adicionar VPC Endpoints (ECR, Secrets Manager, CloudWatch) para reduzir custos
- Implementar Auto Scaling dinâmico baseado em CPU/RAM
- Deploy Blue/Green com AWS CodeDeploy
- Multi-região (disaster recovery)
- Terraform workspaces para múltiplos ambientes (dev, staging, prod)

### Features

- Notificações por email (AWS SES) quando bug for atribuído ou mudança de status
- Upload de screenshots/anexos em bugs (AWS S3 + assinatura pré-assinada)
- Webhooks para integrações externas (Slack, Discord, Microsoft Teams)
- Sistema de tags/labels para bugs (Many-to-Many)
- Dashboard de métricas de bugs (por projeto, tempo médio de resolução, bugs por usuário)
- Histórico de alterações (audit log com Event Sourcing)
- Filtros avançados com QueryDSL ou Spring Data Specifications
- API GraphQL para consultas flexíveis

### Observabilidade

- Distributed tracing com AWS X-Ray ou OpenTelemetry
- Alertas no CloudWatch (bugs críticos, taxa de erro 500, latência alta)
- Log aggregation com ELK Stack (Elasticsearch + Logstash + Kibana)
- Dashboards Grafana customizados (golden signals: latency, traffic, errors, saturation)
- APM (Application Performance Monitoring) com New Relic ou Datadog

### Qualidade e Testes

- Testes E2E com Selenium ou Playwright
- Testes de carga com JMeter ou Gatling
- Análise de vulnerabilidades com Trivy ou Snyk
- SonarQube para qualidade de código
- Mutation testing com PIT
- Aumentar cobertura para 85%+

### Mensageria e Eventos (Branch main)

- Implementar consumidores Kafka (NotificacaoConsumidor, AuditoriaConsumidor)
- Dead Letter Queue (DLQ) com retry policy
- Event Sourcing para auditoria completa
- CQRS (Command Query Responsibility Segregation)
- Integração com AWS EventBridge (branch prod)

---

## Créditos

Desenvolvido e mantido por:

- **Paulo Henrique** - [paulooosf](https://github.com/paulooosf)

Este projeto é um estudo sobre:

- **Arquitetura RESTful** com Spring Boot e boas práticas de design
- **Autenticação JWT** e controle de acesso baseado em roles
- **Event-driven architecture** com Apache Kafka (branch main)
- **Deploy na AWS** com ECS Fargate, RDS, ECR e Application Load Balancer
- **Infraestrutura como código** (IaC) com Terraform
- **CI/CD** com GitHub Actions e rollback automático
- **Containerização** com Docker e Docker Compose
- **Observabilidade** com Actuator, Prometheus, Grafana e CloudWatch
- **Resiliência** e boas práticas de segurança (least privilege, BCrypt, JWT, Security Groups)
- **Testes** unitários e de integração

