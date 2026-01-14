#!/usr/bin/env python3
"""
OpenTelemetry to TimescaleDB Adapter
Recebe métricas via OTLP gRPC e escreve no TimescaleDB
"""

import os
import sys
import logging
import time
from datetime import datetime, timezone
from typing import Optional

import grpc
from opentelemetry.proto.collector.metrics.v1 import metrics_service_pb2, metrics_service_pb2_grpc
from opentelemetry.proto.metrics.v1 import metrics_pb2
from opentelemetry.proto.resource.v1 import resource_pb2
import psycopg2
from psycopg2.extras import execute_values

# Configuração de logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Variáveis de ambiente
OTLP_PORT = int(os.getenv('OTLP_PORT', '4317'))
TIMESCALE_HOST = os.getenv('TIMESCALE_HOST', 'timescaledb')
TIMESCALE_PORT = int(os.getenv('TIMESCALE_PORT', '5432'))
TIMESCALE_DB = os.getenv('TIMESCALE_DB', 'metrics')
TIMESCALE_USER = os.getenv('TIMESCALE_USER', 'metrics')
TIMESCALE_PASSWORD = os.getenv('TIMESCALE_PASSWORD', 'metrics')


class TimescaleDBWriter:
    """Classe para escrever métricas no TimescaleDB"""
    
    def __init__(self):
        self.conn = None
        self._connect()
    
    def _connect(self):
        """Conecta ao TimescaleDB"""
        try:
            self.conn = psycopg2.connect(
                host=TIMESCALE_HOST,
                port=TIMESCALE_PORT,
                database=TIMESCALE_DB,
                user=TIMESCALE_USER,
                password=TIMESCALE_PASSWORD
            )
            logger.info(f"Conectado ao TimescaleDB em {TIMESCALE_HOST}:{TIMESCALE_PORT}")
        except Exception as e:
            logger.error(f"Erro ao conectar ao TimescaleDB: {e}")
            raise
    
    def _ensure_connection(self):
        """Garante que a conexão está ativa"""
        try:
            if self.conn is None or self.conn.closed:
                self._connect()
            else:
                self.conn.rollback()
        except Exception:
            self._connect()
    
    def _extract_resource_attr(self, resource, key: str) -> Optional[str]:
        """Extrai atributo do resource"""
        if resource and resource.attributes:
            for attr in resource.attributes:
                if attr.key == key:
                    return attr.value.string_value if attr.value.HasField('string_value') else None
        return None
    
    def _extract_metric_attr(self, attributes, key: str) -> Optional[str]:
        """Extrai atributo das métricas"""
        if attributes:
            for attr in attributes:
                if attr.key == key:
                    if attr.value.HasField('string_value'):
                        return attr.value.string_value
                    elif attr.value.HasField('int_value'):
                        return str(attr.value.int_value)
                    elif attr.value.HasField('double_value'):
                        return str(attr.value.double_value)
                    elif attr.value.HasField('bool_value'):
                        return str(attr.value.bool_value)
        return None
    
    def _parse_timestamp(self, timestamp_nanos: int) -> datetime:
        """Converte timestamp do OpenTelemetry para datetime"""
        if timestamp_nanos == 0:
            return datetime.now(timezone.utc)
        return datetime.fromtimestamp(timestamp_nanos / 1_000_000_000, tz=timezone.utc)
    
    def write_sum_metrics(self, metric: metrics_pb2.Metric, resource: resource_pb2.Resource):
        """Escreve métricas de contador (sum) no TimescaleDB"""
        if not metric.sum or not metric.sum.data_points:
            return
        
        service_name = self._extract_resource_attr(resource, 'service.name')
        service_namespace = self._extract_resource_attr(resource, 'service.namespace')
        deployment_env = self._extract_resource_attr(resource, 'deployment.environment')
        
        rows = []
        for data_point in metric.sum.data_points:
            timestamp = self._parse_timestamp(data_point.time_unix_nano)
            
            # Extrair atributos da métrica
            service = self._extract_metric_attr(data_point.attributes, 'service')
            product = self._extract_metric_attr(data_point.attributes, 'product')
            environment = self._extract_metric_attr(data_point.attributes, 'environment')
            partner_id = self._extract_metric_attr(data_point.attributes, 'partner_id')
            flow = self._extract_metric_attr(data_point.attributes, 'flow')
            entity_type = self._extract_metric_attr(data_point.attributes, 'entity_type')
            institution_id = self._extract_metric_attr(data_point.attributes, 'institution_id')
            error_type = self._extract_metric_attr(data_point.attributes, 'error_type')
            success_str = self._extract_metric_attr(data_point.attributes, 'success')
            success = success_str.lower() == 'true' if success_str else None
            auth_restriction_type = self._extract_metric_attr(data_point.attributes, 'auth_restriction_type')
            
            value = data_point.as_double if data_point.HasField('as_double') else float(data_point.as_int)
            
            rows.append((
                timestamp,
                metric.name,
                metric.description if metric.description else None,
                metric.unit if metric.unit else None,
                value,
                service_name,
                service_namespace,
                deployment_env,
                service,
                product,
                environment,
                partner_id,
                flow,
                entity_type,
                institution_id,
                error_type,
                success,
                auth_restriction_type,
                metric.sum.is_monotonic,
                metric.sum.aggregation_temporality
            ))
        
        if rows:
            try:
                self._ensure_connection()
                cursor = self.conn.cursor()
                execute_values(
                    cursor,
                    """
                    INSERT INTO otel_metrics_sum (
                        time, metric_name, metric_description, metric_unit, value,
                        service_name, service_namespace, deployment_environment,
                        service, product, environment, partner_id, flow, entity_type,
                        institution_id, error_type, success, auth_restriction_type,
                        is_monotonic, aggregation_temporality
                    ) VALUES %s
                    """,
                    rows
                )
                self.conn.commit()
                cursor.close()
                logger.debug(f"Escritas {len(rows)} métricas sum: {metric.name}")
            except Exception as e:
                logger.error(f"Erro ao escrever métricas sum: {e}")
                self.conn.rollback()
    
    def write_histogram_metrics(self, metric: metrics_pb2.Metric, resource: resource_pb2.Resource):
        """Escreve métricas de histograma no TimescaleDB"""
        if not metric.histogram or not metric.histogram.data_points:
            return
        
        service_name = self._extract_resource_attr(resource, 'service.name')
        service_namespace = self._extract_resource_attr(resource, 'service.namespace')
        deployment_env = self._extract_resource_attr(resource, 'deployment.environment')
        
        rows = []
        for data_point in metric.histogram.data_points:
            timestamp = self._parse_timestamp(data_point.time_unix_nano)
            
            # Extrair atributos da métrica
            service = self._extract_metric_attr(data_point.attributes, 'service')
            product = self._extract_metric_attr(data_point.attributes, 'product')
            environment = self._extract_metric_attr(data_point.attributes, 'environment')
            partner_id = self._extract_metric_attr(data_point.attributes, 'partner_id')
            flow = self._extract_metric_attr(data_point.attributes, 'flow')
            entity_type = self._extract_metric_attr(data_point.attributes, 'entity_type')
            institution_id = self._extract_metric_attr(data_point.attributes, 'institution_id')
            
            rows.append((
                timestamp,
                metric.name,
                metric.description if metric.description else None,
                metric.unit if metric.unit else None,
                data_point.count,
                data_point.sum if data_point.HasField('sum') else None,
                data_point.min if data_point.HasField('min') else None,
                data_point.max if data_point.HasField('max') else None,
                list(data_point.bucket_counts) if data_point.bucket_counts else None,
                list(data_point.explicit_bounds) if data_point.explicit_bounds else None,
                service_name,
                service_namespace,
                deployment_env,
                service,
                product,
                environment,
                partner_id,
                flow,
                entity_type,
                institution_id,
                metric.histogram.aggregation_temporality
            ))
        
        if rows:
            try:
                self._ensure_connection()
                cursor = self.conn.cursor()
                execute_values(
                    cursor,
                    """
                    INSERT INTO otel_metrics_histogram (
                        time, metric_name, metric_description, metric_unit,
                        count, sum, min, max, bucket_counts, explicit_bounds,
                        service_name, service_namespace, deployment_environment,
                        service, product, environment, partner_id, flow, entity_type,
                        institution_id, aggregation_temporality
                    ) VALUES %s
                    """,
                    rows
                )
                self.conn.commit()
                cursor.close()
                logger.debug(f"Escritas {len(rows)} métricas histogram: {metric.name}")
            except Exception as e:
                logger.error(f"Erro ao escrever métricas histogram: {e}")
                self.conn.rollback()
    
    def write_metrics(self, resource_metrics):
        """Processa e escreve métricas no TimescaleDB"""
        for rm in resource_metrics:
            resource = rm.resource
            for scope_metrics in rm.scope_metrics:
                for metric in scope_metrics.metrics:
                    if metric.HasField('sum'):
                        self.write_sum_metrics(metric, resource)
                    elif metric.HasField('histogram'):
                        self.write_histogram_metrics(metric, resource)
                    # Outros tipos de métricas podem ser adicionados aqui


class MetricsService(metrics_service_pb2_grpc.MetricsServiceServicer):
    """Serviço gRPC para receber métricas OTLP"""
    
    def __init__(self, writer: TimescaleDBWriter):
        self.writer = writer
    
    def Export(self, request, context):
        """Endpoint para exportar métricas"""
        try:
            if request.resource_metrics:
                self.writer.write_metrics(request.resource_metrics)
                logger.info(f"Processadas {len(request.resource_metrics)} resource metrics")
            return metrics_service_pb2.ExportMetricsServiceResponse()
        except Exception as e:
            logger.error(f"Erro ao processar métricas: {e}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return metrics_service_pb2.ExportMetricsServiceResponse()


def serve():
    """Inicia o servidor gRPC"""
    from concurrent import futures
    
    writer = TimescaleDBWriter()
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    metrics_service_pb2_grpc.add_MetricsServiceServicer_to_server(
        MetricsService(writer), server
    )
    
    listen_addr = f'0.0.0.0:{OTLP_PORT}'
    server.add_insecure_port(listen_addr)
    server.start()
    logger.info(f"Servidor OTLP iniciado em {listen_addr}")
    
    try:
        while True:
            time.sleep(3600)
    except KeyboardInterrupt:
        logger.info("Parando servidor...")
        server.stop(0)
        writer.conn.close()


if __name__ == '__main__':
    serve()
