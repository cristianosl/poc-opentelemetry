package io.arcotech.poc.metrics.infrastructure.metrics.config

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.metrics.Meter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuração do OpenTelemetry para métricas de produto.
 *
 * Esta configuração utiliza o OpenTelemetry SDK auto-configurado pelo
 * spring-boot-starter que já está configurado via application.yml.
 */
@Configuration
class OpenTelemetryConfig(
    @Value("\${otel.service.name:poc-metrics-backend}")
    private val serviceName: String
) {

    /**
     * Bean do Meter para criação de instrumentos de métricas.
     * O OpenTelemetry é injetado automaticamente pelo spring-boot-starter.
     */
    @Bean
    fun meter(openTelemetry: OpenTelemetry): Meter {
        return openTelemetry.getMeter(serviceName)
    }
}
