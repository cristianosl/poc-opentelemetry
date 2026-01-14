package io.arcotech.poc.metrics.infrastructure.metrics.domain

import io.arcotech.poc.metrics.infrastructure.metrics.ProductMetricsService
import io.arcotech.poc.metrics.infrastructure.metrics.model.Product
import io.arcotech.poc.metrics.infrastructure.metrics.model.ProductMetricType
import org.springframework.stereotype.Component

/**
 * Métricas de envio de dados (Sender).
 *
 * Counters:
 * - integration.sender.webhook.received
 * - integration.sender.webhook.success
 * - integration.sender.webhook.error
 */
@Component
class SenderMetrics(
    private val metricsService: ProductMetricsService
) {

    // ==================== WEBHOOK RECEIVED ====================

    fun recordWebhookReceived(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.SENDER.value, partnerId = partnerId)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Sender.WEBHOOK_RECEIVED,
            attributes,
            "Número de resultados de webhook recebidos"
        )
    }

    // ==================== WEBHOOK SUCCESS ====================

    fun recordWebhookSuccess(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.SENDER.value, partnerId = partnerId, success = true)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Sender.WEBHOOK_SUCCESS,
            attributes,
            "Número de webhooks com status de sucesso"
        )
    }

    // ==================== WEBHOOK ERROR ====================

    fun recordWebhookError(partnerId: String, errorType: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.SENDER.value, partnerId = partnerId, success = false, errorType = errorType)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Sender.WEBHOOK_ERROR,
            attributes,
            "Número de webhooks com status de erro"
        )
    }
}
