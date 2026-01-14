# POC OpenTelemetry Metrics

POC para validar infraestrutura de coleta de métricas com OpenTelemetry, exportando para múltiplos backends com visualização via Metabase.

## Arquitetura

```
┌─────────────┐     ┌───────────────────┐     ┌─────────────┐
│   Backend   │────▶│  OTel Collector   │────▶│  ClickHouse │
│ Spring Boot │     │                   │     └─────────────┘
│   (OTLP)    │     │   (processors)    │
└─────────────┘     │                   │     ┌─────────────┐
                    │                   │────▶│    Mimir    │
                    └───────────────────┘     │ (Prometheus)│
                                              └─────────────┘
                                                     │
┌─────────────┐                               ┌──────▼──────┐
│  Metabase   │◀──────────────────────────────│   Grafana   │
│ (Dashboard) │                               │ (Dashboard) │
└─────────────┘                               └─────────────┘
```

## Stack Tecnológica

| Componente | Versão | Função |
|------------|--------|--------|
| Kotlin | 2.0.21 | Linguagem do backend |
| Java | 21 (Temurin) | Runtime |
| Spring Boot | 3.4.1 | Framework web |
| OpenTelemetry | 1.35.0 | SDK de métricas |
| OTel Collector | 0.96.0 | Coleta e exportação |
| ClickHouse | 24.1 | Banco colunar |
| TimescaleDB | 2.14.0-pg16 | Séries temporais |
| Mimir | 2.11.0 | Backend Prometheus |
| Metabase | 0.48.11 | Visualização |
| Grafana | 10.2.0 | Visualização |

## Pré-requisitos

- Docker e Docker Compose
- Java 21 (para desenvolvimento local)
- Gradle 8.7 (ou use o wrapper)

### Portas Utilizadas

| Porta | Serviço |
|-------|---------|
| 8080 | Backend API |
| 4317 | OTel Collector gRPC |
| 4318 | OTel Collector HTTP |
| 8123 | ClickHouse HTTP |
| 9000 | ClickHouse Native |
| 5432 | TimescaleDB |
| 9009 | Mimir |
| 9001 | MinIO Console |
| 3000 | Metabase |
| 3001 | Grafana |

## Execução

### Via Docker (Recomendado)

```bash
# Subir toda a stack
docker-compose up -d

# Verificar status dos serviços
docker-compose ps

# Ver logs do backend
docker-compose logs -f backend

# Parar a stack
docker-compose down

# Parar e remover volumes
docker-compose down -v
```

### Desenvolvimento Local

```bash
# 1. Subir apenas a infraestrutura
docker-compose up -d clickhouse timescaledb minio mimir otel-collector metabase grafana

# 2. Executar o backend localmente
cd backend
./gradlew bootRun

# Ou com variáveis de ambiente específicas
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 ./gradlew bootRun
```

## Endpoints da API

### Auth - Autenticação

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/auth` | Login SSO v1 |
| POST | `/api/v2/auth` | Login SSO v2 com restrições |
| POST | `/api/v1/auth/token` | Troca opaque token |
| GET | `/api/v1/auth/redirect` | Validação JWT |
| POST | `/api/v1/auth/session` | Validação de sessão |

### Receiver - Recebimento de Entidades

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/institutions/{id}` | Receber instituição |
| POST | `/api/v1/institutions/{id}/classrooms` | Receber turmas |
| POST | `/api/v1/institutions/{id}/admins` | Receber admins |
| POST | `/api/v1/institutions/{id}/coordinators` | Receber coordenadores |
| POST | `/api/v1/institutions/{id}/teachers` | Receber professores |
| POST | `/api/v1/institutions/{id}/students` | Receber alunos |
| POST | `/api/v1/institutions/{id}/licenses` | Receber licenças |

### Sender - Webhooks

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/v1/integrations/results/{syncId}` | Webhook de parceiro |
| POST | `/api/v1/webhooks` | Notificação SNS |

### Utilitários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/health` | Health check |
| GET | `/actuator/prometheus` | Métricas Prometheus |

## Exemplos de Requisições

### Auth V1 - Sucesso

```bash
curl -X POST http://localhost:8080/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "partner_id": "partner-123"
  }'
```

### Auth V1 - Simular Erro

```bash
curl -X POST "http://localhost:8080/api/v1/auth?simulate_error=true" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "partner_id": "partner-123"
  }'
```

### Auth V2 com Restrição

```bash
curl -X POST "http://localhost:8080/api/v2/auth?simulate_delay=100" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "partner_id": "partner-456",
    "restriction_type": "PARTNER_ACCOUNT"
  }'
```

### Receiver - Instituição

```bash
curl -X POST http://localhost:8080/api/v1/institutions/ext-inst-001 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Escola Exemplo",
    "code": "ESC001",
    "partner_id": "partner-123"
  }'
```

### Receiver - Alunos

```bash
curl -X POST http://localhost:8080/api/v1/institutions/ext-inst-001/students \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@example.com",
    "external_id": "student-001",
    "partner_id": "partner-123"
  }'
```

### Sender - Webhook Result

```bash
curl -X POST http://localhost:8080/api/v1/integrations/results/sync-001 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "SUCCESS",
    "partner_id": "partner-123",
    "processed_count": 150
  }'
```

### Sender - Webhook Error

```bash
curl -X POST http://localhost:8080/api/v1/integrations/results/sync-002 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ERROR",
    "error_message": "TimeoutException",
    "partner_id": "partner-123"
  }'
```

## Parâmetros de Simulação

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `simulate_error` | boolean | Força cenário de erro |
| `simulate_delay` | int | Adiciona delay em ms |
| `partner_id` | string | Define partner_id nas métricas |

## URLs de Acesso

| Serviço | URL | Credenciais |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | - |
| Metabase | http://localhost:3000 | Configurar no primeiro acesso |
| Grafana | http://localhost:3001 | admin / admin |
| ClickHouse | http://localhost:8123 | default / (vazio) |
| MinIO Console | http://localhost:9001 | mimir / supersecret |
| Mimir | http://localhost:9009 | - |
| OTel Collector zPages | http://localhost:55679/debug/tracez | - |

## Verificar Métricas

### ClickHouse

```bash
# Conectar ao ClickHouse
docker exec -it poc-clickhouse clickhouse-client

# Listar métricas
SELECT DISTINCT MetricName FROM otel.otel_metrics_sum WHERE MetricName LIKE 'integration.%';

# Ver métricas de auth
SELECT
    MetricName,
    Attributes['partner_id'] as partner_id,
    Attributes['success'] as success,
    Value,
    TimeUnix
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.auth.%'
ORDER BY TimeUnix DESC
LIMIT 20;

# Ver histogramas de duração
SELECT
    MetricName,
    Attributes['partner_id'] as partner_id,
    Count,
    Sum / Count as avg_duration,
    Min,
    Max
FROM otel.otel_metrics_histogram
WHERE MetricName LIKE 'integration.%.duration'
ORDER BY TimeUnix DESC
LIMIT 10;
```

### TimescaleDB

```bash
# Conectar ao TimescaleDB
docker exec -it poc-timescaledb psql -U metrics -d metrics

# Ver métricas recentes
SELECT
    time,
    metric_name,
    product,
    partner_id,
    value
FROM otel_metrics_sum
WHERE metric_name LIKE 'integration.%'
ORDER BY time DESC
LIMIT 20;

# Agregação por minuto
SELECT * FROM product_metrics_summary
ORDER BY bucket DESC
LIMIT 20;
```

### Mimir (PromQL)

> **Importante**: O Mimir está configurado em modo multi-tenant. Todas as queries precisam do header `X-Scope-OrgID: anonymous`.

```bash
# Query simples
curl -H "X-Scope-OrgID: anonymous" \
  -G http://localhost:9009/prometheus/api/v1/query \
  --data-urlencode 'query=integration_auth_redirect_started_total'

# Query com filtro
curl -H "X-Scope-OrgID: anonymous" \
  -G http://localhost:9009/prometheus/api/v1/query \
  --data-urlencode 'query=integration_auth_redirect_started_total{partner_id="partner-123"}'

# Rate de métricas nos últimos 5 minutos
curl -H "X-Scope-OrgID: anonymous" \
  -G http://localhost:9009/prometheus/api/v1/query \
  --data-urlencode 'query=rate(integration_auth_redirect_started_total[5m])'

# Histograma de duração (percentil 95)
curl -H "X-Scope-OrgID: anonymous" \
  -G http://localhost:9009/prometheus/api/v1/query \
  --data-urlencode 'query=histogram_quantile(0.95, integration_auth_redirect_duration_milliseconds_bucket)'

# Listar todas as métricas disponíveis
curl -H "X-Scope-OrgID: anonymous" \
  http://localhost:9009/prometheus/api/v1/label/__name__/values | jq '.data[] | select(startswith("integration"))'
```

## Configurar Metabase

1. Acesse http://localhost:3000
2. Complete o setup inicial
3. Adicione conexão com ClickHouse:
   - Host: `clickhouse`
   - Port: `8123`
   - Database: `otel`
   - User: `default`
4. Adicione conexão com TimescaleDB:
   - Host: `timescaledb`
   - Port: `5432`
   - Database: `metrics`
   - User: `metrics`
   - Password: `metrics`

## Configurar Grafana

1. Acesse http://localhost:3001 (admin/admin)
2. Adicione Data Source Prometheus:
   - URL: `http://mimir:9009/prometheus`
   - Em "Custom HTTP Headers", adicione:
     - Header: `X-Scope-OrgID`
     - Value: `anonymous`
3. Clique em "Save & Test" para verificar a conexão
4. Importe dashboards ou crie queries PromQL

## Executar Testes

```bash
cd backend

# Testes unitários
./gradlew test

# Testes com relatório
./gradlew test --info

# Ver relatório HTML
open build/reports/tests/test/index.html
```

## Catálogo de Métricas

### Auth Metrics (Counters)

| Métrica | Descrição |
|---------|-----------|
| `integration.auth.redirect.started` | Fluxos de redirect iniciados |
| `integration.auth.redirect.completed` | Redirects concluídos |
| `integration.auth.redirect.failed` | Redirects que falharam |
| `integration.auth.token.exchanged` | Tokens trocados |
| `integration.auth.token.validated` | Tokens validados |
| `integration.auth.token.validation_failed` | Validações de token que falharam |
| `integration.auth.opaque_token.generated` | Opaque tokens gerados |
| `integration.auth.opaque_token.validated` | Opaque tokens validados |
| `integration.auth.impersonate.completed` | Impersonates concluídos |
| `integration.auth.impersonate.failed` | Impersonates que falharam |

### Auth Metrics (Histogramas)

| Métrica | Descrição | Unidade |
|---------|-----------|---------|
| `integration.auth.redirect.duration` | Tempo de redirect | ms |

### Receiver Metrics (Counters)

| Métrica | Descrição |
|---------|-----------|
| `integration.receiver.entity.received` | Entidades recebidas |
| `integration.receiver.entity.processed` | Entidades processadas |
| `integration.receiver.entity.failed` | Entidades que falharam |
| `integration.receiver.entity.deleted` | Entidades deletadas |

### Receiver Metrics (Histogramas)

| Métrica | Descrição | Unidade |
|---------|-----------|---------|
| `integration.receiver.processing.duration` | Tempo de processamento | ms |

### Sender Metrics (Counters)

| Métrica | Descrição |
|---------|-----------|
| `integration.sender.webhook.received` | Webhooks recebidos |
| `integration.sender.webhook.success` | Webhooks com sucesso |
| `integration.sender.webhook.error` | Webhooks com erro |

### Atributos das Métricas

| Atributo | Tipo | Obrigatório | Descrição |
|----------|------|-------------|-----------|
| `service` | string | Sim | Nome do serviço |
| `product` | string | Sim | auth, receiver, sender |
| `environment` | string | Sim | local, staging, production |
| `partner_id` | string | Sim | ID do parceiro |
| `flow` | string | Não | redirect, opaque_token, impersonate |
| `entity_type` | string | Não | Student, Teacher, etc. |
| `institution_id` | string | Não | ID da instituição |
| `error_type` | string | Não | Tipo do erro |
| `success` | boolean | Não | Sucesso ou falha |
| `auth_restriction_type` | string | Não | BU, PARTNER_ACCOUNT, PARTNER_INSTITUTION |

## Troubleshooting

### Backend não inicia

```bash
# Verificar logs
docker-compose logs backend

# Verificar se collector está healthy
docker-compose ps otel-collector

# Reiniciar apenas o backend
docker-compose restart backend
```

### Métricas não aparecem no ClickHouse

```bash
# Verificar logs do collector
docker-compose logs otel-collector

# Verificar se ClickHouse está acessível
docker exec poc-otel-collector wget -qO- http://clickhouse:8123/ping

# Verificar se há métricas chegando
docker exec poc-clickhouse clickhouse-client --query "SELECT count() FROM otel.otel_metrics_sum"
```

### Mimir não recebe métricas

```bash
# Verificar logs do Mimir
docker-compose logs mimir

# Verificar se MinIO está ok
docker-compose logs minio

# Verificar buckets
docker exec poc-minio-init mc ls myminio/
```

### Limpeza completa

```bash
# Parar tudo e remover volumes
docker-compose down -v

# Remover imagens
docker-compose down --rmi local

# Rebuild completo
docker-compose build --no-cache
docker-compose up -d
```

## Estrutura do Projeto

```
poc-opentelemetry/
├── backend/
│   ├── src/main/kotlin/io/arcotech/poc/metrics/
│   │   ├── controller/          # Controllers REST
│   │   ├── service/             # Services de simulação
│   │   ├── dto/                 # DTOs request/response
│   │   └── infrastructure/
│   │       └── metrics/
│   │           ├── config/      # Configuração OTel
│   │           ├── model/       # Atributos e tipos
│   │           └── domain/      # Métricas de domínio
│   ├── src/test/kotlin/         # Testes unitários
│   ├── build.gradle.kts
│   └── Dockerfile
├── otel-collector/
│   └── config.yaml              # Configuração do Collector
├── mimir/
│   └── config.yaml              # Configuração do Mimir
├── databases/
│   ├── clickhouse/
│   │   └── init.sql             # Schema ClickHouse
│   └── timescaledb/
│       └── init.sql             # Schema TimescaleDB
├── docker-compose.yml
└── README.md
```

## Referências

- [OpenTelemetry SDK](https://opentelemetry.io/docs/instrumentation/java/)
- [OpenTelemetry Collector](https://opentelemetry.io/docs/collector/)
- [ClickHouse Documentation](https://clickhouse.com/docs)
- [TimescaleDB Documentation](https://docs.timescale.com/)
- [Mimir Documentation](https://grafana.com/docs/mimir/latest/)
- [Metabase Documentation](https://www.metabase.com/docs/latest/)
