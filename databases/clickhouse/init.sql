-- ClickHouse schema for OpenTelemetry metrics

CREATE DATABASE IF NOT EXISTS otel;

-- Tabela para métricas de gauge/sum (counters)
CREATE TABLE IF NOT EXISTS otel.otel_metrics_sum
(
    ResourceAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ResourceSchemaUrl String CODEC(ZSTD(1)),
    ScopeName String CODEC(ZSTD(1)),
    ScopeVersion String CODEC(ZSTD(1)),
    ScopeAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ScopeDroppedAttrCount UInt32 CODEC(ZSTD(1)),
    ScopeSchemaUrl String CODEC(ZSTD(1)),
    MetricName LowCardinality(String) CODEC(ZSTD(1)),
    MetricDescription String CODEC(ZSTD(1)),
    MetricUnit String CODEC(ZSTD(1)),
    Attributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    StartTimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    TimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    Value Float64 CODEC(ZSTD(1)),
    Flags UInt32 CODEC(ZSTD(1)),
    AggregationTemporality Int32 CODEC(ZSTD(1)),
    IsMonotonic Bool CODEC(ZSTD(1))
)
ENGINE = MergeTree()
PARTITION BY toDate(TimeUnix)
ORDER BY (MetricName, toUnixTimestamp64Nano(TimeUnix))
TTL toDateTime(TimeUnix) + INTERVAL 30 DAY;

-- Tabela para métricas de histograma
CREATE TABLE IF NOT EXISTS otel.otel_metrics_histogram
(
    ResourceAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ResourceSchemaUrl String CODEC(ZSTD(1)),
    ScopeName String CODEC(ZSTD(1)),
    ScopeVersion String CODEC(ZSTD(1)),
    ScopeAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ScopeDroppedAttrCount UInt32 CODEC(ZSTD(1)),
    ScopeSchemaUrl String CODEC(ZSTD(1)),
    MetricName LowCardinality(String) CODEC(ZSTD(1)),
    MetricDescription String CODEC(ZSTD(1)),
    MetricUnit String CODEC(ZSTD(1)),
    Attributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    StartTimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    TimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    Count UInt64 CODEC(ZSTD(1)),
    Sum Float64 CODEC(ZSTD(1)),
    BucketCounts Array(UInt64) CODEC(ZSTD(1)),
    ExplicitBounds Array(Float64) CODEC(ZSTD(1)),
    Min Float64 CODEC(ZSTD(1)),
    Max Float64 CODEC(ZSTD(1)),
    Flags UInt32 CODEC(ZSTD(1)),
    AggregationTemporality Int32 CODEC(ZSTD(1))
)
ENGINE = MergeTree()
PARTITION BY toDate(TimeUnix)
ORDER BY (MetricName, toUnixTimestamp64Nano(TimeUnix))
TTL toDateTime(TimeUnix) + INTERVAL 30 DAY;

-- Tabela para métricas de gauge
CREATE TABLE IF NOT EXISTS otel.otel_metrics_gauge
(
    ResourceAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ResourceSchemaUrl String CODEC(ZSTD(1)),
    ScopeName String CODEC(ZSTD(1)),
    ScopeVersion String CODEC(ZSTD(1)),
    ScopeAttributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    ScopeDroppedAttrCount UInt32 CODEC(ZSTD(1)),
    ScopeSchemaUrl String CODEC(ZSTD(1)),
    MetricName LowCardinality(String) CODEC(ZSTD(1)),
    MetricDescription String CODEC(ZSTD(1)),
    MetricUnit String CODEC(ZSTD(1)),
    Attributes Map(LowCardinality(String), String) CODEC(ZSTD(1)),
    StartTimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    TimeUnix DateTime64(9) CODEC(Delta, ZSTD(1)),
    Value Float64 CODEC(ZSTD(1)),
    Flags UInt32 CODEC(ZSTD(1))
)
ENGINE = MergeTree()
PARTITION BY toDate(TimeUnix)
ORDER BY (MetricName, toUnixTimestamp64Nano(TimeUnix))
TTL toDateTime(TimeUnix) + INTERVAL 30 DAY;

-- View materializada para facilitar queries de métricas de produto
CREATE VIEW IF NOT EXISTS otel.product_metrics_view AS
SELECT
    MetricName,
    Attributes['service'] AS service,
    Attributes['product'] AS product,
    Attributes['environment'] AS environment,
    Attributes['partner_id'] AS partner_id,
    Attributes['entity_type'] AS entity_type,
    Attributes['flow'] AS flow,
    Attributes['success'] AS success,
    Attributes['error_type'] AS error_type,
    Value,
    TimeUnix
FROM otel.otel_metrics_sum
WHERE MetricName LIKE 'integration.%';

-- View para histogramas de produto
CREATE VIEW IF NOT EXISTS otel.product_histograms_view AS
SELECT
    MetricName,
    Attributes['service'] AS service,
    Attributes['product'] AS product,
    Attributes['environment'] AS environment,
    Attributes['partner_id'] AS partner_id,
    Attributes['entity_type'] AS entity_type,
    Count,
    Sum,
    Min,
    Max,
    Sum / Count AS avg_duration,
    TimeUnix
FROM otel.otel_metrics_histogram
WHERE MetricName LIKE 'integration.%';
