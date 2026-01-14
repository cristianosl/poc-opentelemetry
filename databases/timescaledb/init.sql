-- TimescaleDB schema for OpenTelemetry metrics

-- Habilitar extensão TimescaleDB
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- Tabela para métricas de contador (sum)
CREATE TABLE IF NOT EXISTS otel_metrics_sum (
    time TIMESTAMPTZ NOT NULL,
    metric_name TEXT NOT NULL,
    metric_description TEXT,
    metric_unit TEXT,
    value DOUBLE PRECISION NOT NULL,
    -- Resource attributes
    service_name TEXT,
    service_namespace TEXT,
    deployment_environment TEXT,
    -- Metric attributes
    service TEXT,
    product TEXT,
    environment TEXT,
    partner_id TEXT,
    flow TEXT,
    entity_type TEXT,
    institution_id TEXT,
    error_type TEXT,
    success BOOLEAN,
    auth_restriction_type TEXT,
    -- Metadata
    is_monotonic BOOLEAN DEFAULT TRUE,
    aggregation_temporality INT DEFAULT 2
);

-- Converter para hypertable
SELECT create_hypertable('otel_metrics_sum', 'time', if_not_exists => TRUE);

-- Índices para consultas comuns
CREATE INDEX IF NOT EXISTS idx_metrics_sum_name ON otel_metrics_sum (metric_name, time DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_sum_product ON otel_metrics_sum (product, time DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_sum_partner ON otel_metrics_sum (partner_id, time DESC);

-- Tabela para métricas de histograma
CREATE TABLE IF NOT EXISTS otel_metrics_histogram (
    time TIMESTAMPTZ NOT NULL,
    metric_name TEXT NOT NULL,
    metric_description TEXT,
    metric_unit TEXT,
    count BIGINT NOT NULL,
    sum DOUBLE PRECISION,
    min DOUBLE PRECISION,
    max DOUBLE PRECISION,
    bucket_counts BIGINT[],
    explicit_bounds DOUBLE PRECISION[],
    -- Resource attributes
    service_name TEXT,
    service_namespace TEXT,
    deployment_environment TEXT,
    -- Metric attributes
    service TEXT,
    product TEXT,
    environment TEXT,
    partner_id TEXT,
    flow TEXT,
    entity_type TEXT,
    institution_id TEXT,
    -- Metadata
    aggregation_temporality INT DEFAULT 2
);

-- Converter para hypertable
SELECT create_hypertable('otel_metrics_histogram', 'time', if_not_exists => TRUE);

-- Índices para consultas comuns
CREATE INDEX IF NOT EXISTS idx_metrics_hist_name ON otel_metrics_histogram (metric_name, time DESC);
CREATE INDEX IF NOT EXISTS idx_metrics_hist_product ON otel_metrics_histogram (product, time DESC);

-- Políticas de retenção (30 dias)
SELECT add_retention_policy('otel_metrics_sum', INTERVAL '30 days', if_not_exists => TRUE);
SELECT add_retention_policy('otel_metrics_histogram', INTERVAL '30 days', if_not_exists => TRUE);

-- View para facilitar consultas de métricas de produto
CREATE OR REPLACE VIEW product_metrics_summary AS
SELECT
    time_bucket('1 minute', time) AS bucket,
    metric_name,
    product,
    partner_id,
    entity_type,
    flow,
    success,
    SUM(value) AS total_count,
    COUNT(*) AS data_points
FROM otel_metrics_sum
WHERE metric_name LIKE 'integration.%'
GROUP BY bucket, metric_name, product, partner_id, entity_type, flow, success;

-- View para histogramas de produto
CREATE OR REPLACE VIEW product_histograms_summary AS
SELECT
    time_bucket('1 minute', time) AS bucket,
    metric_name,
    product,
    partner_id,
    entity_type,
    SUM(count) AS total_count,
    SUM(sum) / NULLIF(SUM(count), 0) AS avg_duration,
    MIN(min) AS min_duration,
    MAX(max) AS max_duration
FROM otel_metrics_histogram
WHERE metric_name LIKE 'integration.%'
GROUP BY bucket, metric_name, product, partner_id, entity_type;

-- Continuous aggregates para dashboards (opcional, melhora performance)
CREATE MATERIALIZED VIEW IF NOT EXISTS product_metrics_hourly
WITH (timescaledb.continuous) AS
SELECT
    time_bucket('1 hour', time) AS bucket,
    metric_name,
    product,
    partner_id,
    SUM(value) AS total_count
FROM otel_metrics_sum
WHERE metric_name LIKE 'integration.%'
GROUP BY bucket, metric_name, product, partner_id
WITH NO DATA;

-- Política de refresh para continuous aggregate
SELECT add_continuous_aggregate_policy('product_metrics_hourly',
    start_offset => INTERVAL '3 hours',
    end_offset => INTERVAL '1 hour',
    schedule_interval => INTERVAL '1 hour',
    if_not_exists => TRUE);
