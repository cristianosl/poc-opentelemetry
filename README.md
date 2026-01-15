# POC OpenTelemetry Metrics

POC para validar infraestrutura de coleta de métricas com OpenTelemetry, exportando para múltiplos backends (ClickHouse, Mimir e TimescaleDB) com visualização via Metabase e Grafana.

## Arquitetura

```
┌─────────────┐     ┌───────────────────┐     ┌─────────────┐
│   Backend   │────▶│  OTel Collector   │────▶│  ClickHouse │
│ Spring Boot │     │                   │     └─────────────┘
│   (OTLP)    │     │   (processors)    │
└─────────────┘     │                   │     ┌─────────────┐
                    │                   │────▶│    Mimir    │
                    │                   │     │ (Prometheus)│
                    │                   │     └─────────────┘
                    │                   │
                    │                   │     ┌──────────────────┐
                    │                   │────▶│ TimescaleDB      │
                    └───────────────────┘     │ Adapter (Python) │
                                              └──────────┬───────┘
                                                         │
                                              ┌──────────▼───────┐
                                              │   TimescaleDB    │
                                              └──────────────────┘
                                                         │
┌─────────────┐                               ┌──────────┴───────┐
│  Metabase   │◀──────────────────────────────│   Grafana        │
│ (Dashboard) │                               │ (Dashboard)      │
└─────────────┘                               └──────────────────┘
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
| Python | 3.11 | Adapter TimescaleDB |
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
| 4319 | TimescaleDB Adapter gRPC |
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
docker-compose up -d clickhouse timescaledb minio mimir otel-collector otel-timescale-adapter metabase grafana

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

## Como Funciona a Integração com TimescaleDB

O OpenTelemetry Collector não possui um exportador nativo para PostgreSQL/TimescaleDB. Para resolver isso, foi criado um serviço intermediário (`otel-timescale-adapter`) que:

1. **Recebe métricas via OTLP gRPC** na porta 4317
2. **Transforma os dados** do formato OpenTelemetry para o schema do TimescaleDB
3. **Escreve no TimescaleDB** usando inserções em batch para melhor performance

O adapter suporta:
- **Métricas de contador (sum)**: Armazenadas na tabela `otel_metrics_sum`
- **Métricas de histograma**: Armazenadas na tabela `otel_metrics_histogram`

O adapter é implementado em Python e utiliza:
- `opentelemetry-proto` para deserializar dados OTLP
- `grpcio` para o servidor gRPC
- `psycopg2` para conexão com PostgreSQL/TimescaleDB

## Verificar Métricas

Esta seção apresenta queries equivalentes nos três backends para facilitar a comparação de sintaxe.

> **Nota sobre valores diferentes**: Os valores retornados podem diferir entre backends devido à forma de armazenamento:
> - **ClickHouse/TimescaleDB**: Armazenam cada ponto de dados exportado (soma acumulada de todos os exports)
> - **Mimir**: Armazena séries temporais Prometheus (valor atual do contador)

### Conexão aos Bancos

```bash
# ClickHouse
docker exec -it poc-clickhouse clickhouse-client

# TimescaleDB
docker exec -it poc-timescaledb psql -U metrics -d metrics

# Mimir (via curl com header obrigatório)
curl -s -H 'X-Scope-OrgID: anonymous' 'http://localhost:9009/prometheus/api/v1/query?query=...'
```

---

### Query 1: Listar métricas disponíveis

**ClickHouse:**
```sql
SELECT DISTINCT MetricName
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.%'
ORDER BY MetricName;
```

**TimescaleDB:**
```sql
SELECT DISTINCT metric_name
FROM otel_metrics_sum
WHERE metric_name LIKE 'integration.%'
ORDER BY metric_name;
```

**Mimir (PromQL):**
```bash
curl -s -H 'X-Scope-OrgID: anonymous' \
  'http://localhost:9009/prometheus/api/v1/label/__name__/values' | \
  grep -o '"integration[^"]*"'
```

---

### Query 2: Total de redirects por partner

**ClickHouse:**
```sql
SELECT
    Attributes['partner_id'] AS partner_id,
    sum(Value) AS total
FROM otel.otel_metrics_sum
WHERE MetricName = 'integration.auth.redirect.started'
GROUP BY partner_id
ORDER BY total DESC;
```

**TimescaleDB:**
```sql
SELECT
    partner_id,
    sum(value) AS total
FROM otel_metrics_sum
WHERE metric_name = 'integration.auth.redirect.started'
GROUP BY partner_id
ORDER BY total DESC;
```

**Mimir (PromQL):**
```bash
# Valor atual por partner
curl -s -H 'X-Scope-OrgID: anonymous' \
  'http://localhost:9009/prometheus/api/v1/query?query=integration_auth_redirect_started_total'

# Total agregado (soma de todos partners)
curl -s -H 'X-Scope-OrgID: anonymous' \
  'http://localhost:9009/prometheus/api/v1/query?query=sum(integration_auth_redirect_started_total)'
```

---

### Query 3: Contagem de webhooks por status (success vs error)

**ClickHouse:**
```sql
SELECT
    MetricName,
    sum(Value) AS total
FROM otel.otel_metrics_sum
WHERE MetricName IN (
    'integration.sender.webhook.success',
    'integration.sender.webhook.error'
)
GROUP BY MetricName;
```

**TimescaleDB:**
```sql
SELECT
    metric_name,
    sum(value) AS total
FROM otel_metrics_sum
WHERE metric_name IN (
    'integration.sender.webhook.success',
    'integration.sender.webhook.error'
)
GROUP BY metric_name;
```

**Mimir (PromQL):**
```bash
# Success
curl -s -H 'X-Scope-OrgID: anonymous' \
  'http://localhost:9009/prometheus/api/v1/query?query=sum(integration_sender_webhook_success_total)'

# Error
curl -s -H 'X-Scope-OrgID: anonymous' \
  'http://localhost:9009/prometheus/api/v1/query?query=sum(integration_sender_webhook_error_total)'

# Ambos em uma query (por métrica)
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query={__name__=~"integration_sender_webhook_(success|error)_total"}'
```

---

### Query 4: Estatísticas de duração do redirect (histograma)

**ClickHouse:**
```sql
SELECT
    Attributes['partner_id'] AS partner_id,
    sum(Count) AS total_requests,
    sum(Sum) / sum(Count) AS avg_ms,
    min(Min) AS min_ms,
    max(Max) AS max_ms
FROM otel.otel_metrics_histogram
WHERE MetricName = 'integration.auth.redirect.duration'
GROUP BY partner_id
ORDER BY total_requests DESC;
```

**TimescaleDB:**
```sql
SELECT
    partner_id,
    sum(count) AS total_requests,
    sum(sum) / NULLIF(sum(count), 0) AS avg_ms,
    min(min) AS min_ms,
    max(max) AS max_ms
FROM otel_metrics_histogram
WHERE metric_name = 'integration.auth.redirect.duration'
GROUP BY partner_id
ORDER BY total_requests DESC;
```

**Mimir (PromQL):**
```bash
# Média de duração
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query=sum(integration_auth_redirect_duration_milliseconds_sum) / sum(integration_auth_redirect_duration_milliseconds_count)'

# Percentil 50 (mediana)
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query=histogram_quantile(0.50, rate(integration_auth_redirect_duration_milliseconds_bucket[5m]))'

# Percentil 95
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query=histogram_quantile(0.95, rate(integration_auth_redirect_duration_milliseconds_bucket[5m]))'

# Percentil 99
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query=histogram_quantile(0.99, rate(integration_auth_redirect_duration_milliseconds_bucket[5m]))'
```

---

### Query 5: Métricas por produto (auth, receiver, sender)

**ClickHouse:**
```sql
SELECT
    Attributes['product'] AS product,
    MetricName,
    sum(Value) AS total
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.%'
GROUP BY product, MetricName
ORDER BY product, total DESC;
```

**TimescaleDB:**
```sql
SELECT
    product,
    metric_name,
    sum(value) AS total
FROM otel_metrics_sum
WHERE metric_name LIKE 'integration.%'
GROUP BY product, metric_name
ORDER BY product, total DESC;
```

**Mimir (PromQL):**
```bash
# Todas métricas de auth
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query={__name__=~"integration_auth_.+", product="auth"}'

# Todas métricas de receiver
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query={__name__=~"integration_receiver_.+", product="receiver"}'

# Todas métricas de sender
curl -s -H 'X-Scope-OrgID: anonymous' \
  -G 'http://localhost:9009/prometheus/api/v1/query' \
  --data-urlencode 'query={__name__=~"integration_sender_.+", product="sender"}'
```

---

### Comparativo de Sintaxe

| Operação | ClickHouse | TimescaleDB | Mimir (PromQL) |
|----------|------------|-------------|----------------|
| Acessar atributo | `Attributes['key']` | `column_name` | `{label="value"}` |
| Filtro de nome | `WHERE MetricName = '...'` | `WHERE metric_name = '...'` | `metric_name{...}` |
| Agregação | `sum(Value)` | `sum(value)` | `sum(metric)` |
| Group by | `GROUP BY field` | `GROUP BY field` | `sum by (label)` |
| Wildcard | `LIKE 'prefix.%'` | `LIKE 'prefix.%'` | `{__name__=~"prefix.+"}` |
| Histograma avg | `Sum / Count` | `sum / count` | `_sum / _count` |
| Percentil | N/A (calcular buckets) | N/A (calcular buckets) | `histogram_quantile(0.95, ...)` |

## Configurar Metabase

### Primeiro Acesso

1. Acesse http://localhost:3000
2. Complete o setup inicial criando uma conta de administrador
3. Na tela de adicionar banco de dados, você pode pular e configurar depois

### Driver ClickHouse

O driver ClickHouse para Metabase é instalado automaticamente pelo container `metabase-clickhouse-driver` na inicialização. Não é necessária nenhuma configuração adicional.

> **Nota**: O driver é baixado do repositório oficial [ClickHouse/metabase-clickhouse-driver](https://github.com/ClickHouse/metabase-clickhouse-driver) versão 1.50.0.

### Adicionar Conexão ClickHouse

1. Vá em **Settings** (engrenagem) > **Admin settings** > **Databases** > **Add database**
2. Selecione **ClickHouse** no dropdown
3. Configure:
   - **Display name**: `ClickHouse OTEL`
   - **Host**: `clickhouse`
   - **Port**: `8123`
   - **Database name**: `otel`
   - **Username**: `default`
   - **Password**: (deixe vazio)
4. Clique em **Save**

### Adicionar Conexão TimescaleDB

1. Vá em **Settings** > **Admin settings** > **Databases** > **Add database**
2. Selecione **PostgreSQL** no dropdown
3. Configure:
   - **Display name**: `TimescaleDB Metrics`
   - **Host**: `timescaledb`
   - **Port**: `5432`
   - **Database name**: `metrics`
   - **Username**: `metrics`
   - **Password**: `metrics`
4. Clique em **Save**

### Queries de Exemplo no Metabase

Após adicionar os bancos de dados, você pode criar queries nativas:

**Total de métricas por tipo (ClickHouse):**
```sql
SELECT
    MetricName,
    count() as total,
    max(TimeUnix) as last_seen
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.%'
GROUP BY MetricName
ORDER BY total DESC
```

**Métricas por parceiro (ClickHouse):**
```sql
SELECT
    Attributes['partner_id'] as partner_id,
    MetricName,
    sum(Value) as total
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.auth.%'
GROUP BY partner_id, MetricName
ORDER BY partner_id, total DESC
```

## Configurar Grafana

O Grafana já vem **pré-configurado** com o datasource do Mimir. Basta acessar:

1. Acesse http://localhost:3001 (admin/admin)
2. O datasource "Mimir" já está configurado e pronto para uso
3. Crie dashboards ou use o Explore para queries PromQL

### Exemplo de Query no Explore

```promql
# Total de redirects por partner
integration_auth_redirect_started_total

# Taxa de erros nos últimos 5 minutos
rate(integration_auth_redirect_failed_total[5m])

# Percentil 95 de duração
histogram_quantile(0.95, rate(integration_auth_redirect_duration_milliseconds_bucket[5m]))
```

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

# Verificar se há métricas chegando
docker exec poc-clickhouse clickhouse-client --query "SELECT count() FROM otel.otel_metrics_sum"

# Ver últimas métricas
docker exec poc-clickhouse clickhouse-client --query "SELECT MetricName, count() FROM otel.otel_metrics_sum GROUP BY MetricName"
```

### Mimir não recebe métricas

```bash
# Verificar logs do Mimir
docker-compose logs mimir

# Verificar logs do collector (erros de exportação)
docker-compose logs otel-collector | grep -i "error.*prometheus"

# Verificar se MinIO está ok
docker-compose logs minio

# Testar conexão com Mimir (requer header X-Scope-OrgID)
curl -s -H 'X-Scope-OrgID: anonymous' 'http://localhost:9009/prometheus/api/v1/label/__name__/values'
```

### Métricas não aparecem no TimescaleDB

```bash
# Verificar logs do adapter
docker-compose logs otel-timescale-adapter

# Verificar se o adapter está rodando
docker-compose ps otel-timescale-adapter

# Verificar logs do collector (erros de exportação para TimescaleDB)
docker-compose logs otel-collector | grep -i "timescale\|error.*otlp"

# Verificar se há métricas no banco
docker exec poc-timescaledb psql -U metrics -d metrics -c "SELECT COUNT(*) FROM otel_metrics_sum;"

# Verificar conexão do adapter com TimescaleDB
docker-compose logs otel-timescale-adapter | grep -i "conectado\|erro"

# Reiniciar o adapter se necessário
docker-compose restart otel-timescale-adapter
```

### Metabase não mostra ClickHouse como opção

```bash
# Verificar se o driver foi baixado
docker exec poc-metabase ls -la /plugins/

# Verificar logs de carregamento do driver
docker-compose logs metabase | grep -i clickhouse

# Se não houver driver, recriar o container
docker-compose rm -f metabase metabase-clickhouse-driver
docker volume rm poc-opentelemetry_metabase-plugins
docker-compose up -d metabase
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
├── otel-timescale-adapter/
│   ├── adapter.py               # Serviço Python que recebe OTLP e escreve no TimescaleDB
│   ├── requirements.txt         # Dependências Python
│   └── Dockerfile               # Imagem Docker do adapter
├── grafana/
│   └── provisioning/
│       └── datasources/
│           └── mimir.yaml       # Datasource Mimir pré-configurado
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
