package io.arcotech.poc.metrics.infrastructure.metrics

import io.arcotech.poc.metrics.infrastructure.metrics.model.MetricAttributesBuilder
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * Serviço centralizado para emissão de métricas de produto.
 * Gerencia a criação e cache de instrumentos de métricas (counters e histogramas).
 */
@Service
class ProductMetricsService(
    private val meter: Meter,
    @Value("\${otel.service.name:poc-metrics-backend}")
    private val serviceName: String,
    @Value("\${otel.resource.attributes.deployment.environment:local}")
    private val environment: String
) {
    private val counters = ConcurrentHashMap<String, LongCounter>()
    private val histograms = ConcurrentHashMap<String, DoubleHistogram>()

    /**
     * Obtém ou cria um counter com o nome especificado.
     */
    fun getCounter(name: String, description: String = ""): LongCounter {
        return counters.computeIfAbsent(name) {
            meter.counterBuilder(name)
                .setDescription(description)
                .setUnit("1")
                .build()
        }
    }

    /**
     * Obtém ou cria um histograma com o nome especificado.
     */
    fun getHistogram(name: String, description: String = "", unit: String = "ms"): DoubleHistogram {
        return histograms.computeIfAbsent(name) {
            meter.histogramBuilder(name)
                .setDescription(description)
                .setUnit(unit)
                .build()
        }
    }

    /**
     * Incrementa um counter com os atributos fornecidos.
     */
    fun incrementCounter(name: String, attributes: Attributes, description: String = "") {
        getCounter(name, description).add(1, attributes)
    }

    /**
     * Registra um valor no histograma com os atributos fornecidos.
     */
    fun recordHistogram(name: String, value: Double, attributes: Attributes, description: String = "", unit: String = "ms") {
        getHistogram(name, description, unit).record(value, attributes)
    }

    /**
     * Cria um builder de atributos pré-configurado com service e environment.
     */
    fun attributesBuilder(): MetricAttributesBuilder {
        return MetricAttributesBuilder(
            service = serviceName,
            environment = environment
        )
    }
}
