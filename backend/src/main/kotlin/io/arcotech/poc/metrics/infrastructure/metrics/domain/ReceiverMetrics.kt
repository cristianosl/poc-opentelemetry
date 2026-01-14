package io.arcotech.poc.metrics.infrastructure.metrics.domain

import io.arcotech.poc.metrics.infrastructure.metrics.ProductMetricsService
import io.arcotech.poc.metrics.infrastructure.metrics.model.EntityType
import io.arcotech.poc.metrics.infrastructure.metrics.model.Product
import io.arcotech.poc.metrics.infrastructure.metrics.model.ProductMetricType
import org.springframework.stereotype.Component

/**
 * Métricas de recebimento de entidades (Receiver).
 *
 * Counters:
 * - integration.receiver.entity.received
 * - integration.receiver.entity.processed
 * - integration.receiver.entity.failed
 * - integration.receiver.entity.deleted
 *
 * Histogramas:
 * - integration.receiver.processing.duration
 */
@Component
class ReceiverMetrics(
    private val metricsService: ProductMetricsService
) {

    // ==================== ENTITY RECEIVED ====================

    fun recordEntityReceived(partnerId: String, entityType: EntityType, institutionId: String? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.RECEIVER.value,
                partnerId = partnerId,
                entityType = entityType.value,
                institutionId = institutionId
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Receiver.ENTITY_RECEIVED,
            attributes,
            "Número de entidades recebidas"
        )
    }

    // ==================== ENTITY PROCESSED ====================

    fun recordEntityProcessed(partnerId: String, entityType: EntityType, institutionId: String? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.RECEIVER.value,
                partnerId = partnerId,
                entityType = entityType.value,
                institutionId = institutionId,
                success = true
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Receiver.ENTITY_PROCESSED,
            attributes,
            "Número de entidades processadas com sucesso"
        )
    }

    // ==================== ENTITY FAILED ====================

    fun recordEntityFailed(partnerId: String, entityType: EntityType, errorType: String, institutionId: String? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.RECEIVER.value,
                partnerId = partnerId,
                entityType = entityType.value,
                institutionId = institutionId,
                success = false,
                errorType = errorType
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Receiver.ENTITY_FAILED,
            attributes,
            "Número de entidades que falharam no processamento"
        )
    }

    // ==================== ENTITY DELETED ====================

    fun recordEntityDeleted(partnerId: String, entityType: EntityType, institutionId: String? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.RECEIVER.value,
                partnerId = partnerId,
                entityType = entityType.value,
                institutionId = institutionId
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Receiver.ENTITY_DELETED,
            attributes,
            "Número de entidades deletadas"
        )
    }

    // ==================== PROCESSING DURATION ====================

    fun recordProcessingDuration(partnerId: String, entityType: EntityType, durationMs: Double, institutionId: String? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.RECEIVER.value,
                partnerId = partnerId,
                entityType = entityType.value,
                institutionId = institutionId
            )
            .build()
        metricsService.recordHistogram(
            ProductMetricType.Receiver.PROCESSING_DURATION,
            durationMs,
            attributes,
            "Tempo de processamento de entidade",
            "ms"
        )
    }
}
