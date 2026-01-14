package io.arcotech.poc.metrics.infrastructure.metrics

import io.arcotech.poc.metrics.infrastructure.metrics.domain.SenderMetrics
import io.mockk.*
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

/**
 * Testes unitários para SenderMetrics.
 * Valida que as métricas são emitidas corretamente com os atributos esperados.
 */
class SenderMetricsTest {

    private lateinit var meter: Meter
    private lateinit var metricsService: ProductMetricsService
    private lateinit var senderMetrics: SenderMetrics
    private lateinit var mockCounter: LongCounter

    @BeforeEach
    fun setup() {
        meter = mockk(relaxed = true)
        mockCounter = mockk(relaxed = true)

        every { meter.counterBuilder(any()).setDescription(any()).setUnit(any()).build() } returns mockCounter

        metricsService = ProductMetricsService(meter, "poc-metrics-backend", "local")
        senderMetrics = SenderMetrics(metricsService)
    }

    @Test
    @DisplayName("Deve emitir métrica webhook.received")
    fun shouldRecordWebhookReceived() {
        // Given
        val partnerId = "partner-123"

        // When
        senderMetrics.recordWebhookReceived(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("product")) == "sender"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica webhook.success com success=true")
    fun shouldRecordWebhookSuccess() {
        // Given
        val partnerId = "partner-123"

        // When
        senderMetrics.recordWebhookSuccess(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == true
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica webhook.error com error_type")
    fun shouldRecordWebhookError() {
        // Given
        val partnerId = "partner-123"
        val errorType = "TimeoutException"

        // When
        senderMetrics.recordWebhookError(partnerId, errorType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("error_type")) == errorType &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == false
            })
        }
    }

    @Test
    @DisplayName("Deve emitir todas as métricas de sender em sequência")
    fun shouldRecordAllSenderMetricsInSequence() {
        // Given
        val partnerId = "partner-123"

        // When - Simula um fluxo completo de webhook
        senderMetrics.recordWebhookReceived(partnerId)
        senderMetrics.recordWebhookSuccess(partnerId)

        // Then
        verify(exactly = 2) {
            mockCounter.add(1, any())
        }
    }

    @Test
    @DisplayName("Deve emitir métricas de erro com diferentes tipos de erro")
    fun shouldRecordWebhookErrorWithDifferentErrorTypes() {
        // Given
        val partnerId = "partner-123"
        val errorTypes = listOf("TimeoutException", "ConnectionException", "ValidationException", "UnknownError")

        // When
        errorTypes.forEach { errorType ->
            senderMetrics.recordWebhookError(partnerId, errorType)
        }

        // Then
        verify(exactly = errorTypes.size) {
            mockCounter.add(1, any())
        }
    }
}
