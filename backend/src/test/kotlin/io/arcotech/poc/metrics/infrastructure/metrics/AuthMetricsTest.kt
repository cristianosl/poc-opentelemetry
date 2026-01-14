package io.arcotech.poc.metrics.infrastructure.metrics

import io.arcotech.poc.metrics.infrastructure.metrics.domain.AuthMetrics
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthFlow
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthRestrictionType
import io.arcotech.poc.metrics.infrastructure.metrics.model.ProductMetricType
import io.mockk.*
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

/**
 * Testes unitários para AuthMetrics.
 * Valida que as métricas são emitidas corretamente com os atributos esperados.
 */
class AuthMetricsTest {

    private lateinit var meter: Meter
    private lateinit var metricsService: ProductMetricsService
    private lateinit var authMetrics: AuthMetrics
    private lateinit var mockCounter: LongCounter
    private lateinit var mockHistogram: DoubleHistogram

    @BeforeEach
    fun setup() {
        meter = mockk(relaxed = true)
        mockCounter = mockk(relaxed = true)
        mockHistogram = mockk(relaxed = true)

        every { meter.counterBuilder(any()).setDescription(any()).setUnit(any()).build() } returns mockCounter
        every { meter.histogramBuilder(any()).setDescription(any()).setUnit(any()).build() } returns mockHistogram

        metricsService = ProductMetricsService(meter, "poc-metrics-backend", "local")
        authMetrics = AuthMetrics(metricsService)
    }

    @Test
    @DisplayName("Deve emitir métrica redirect.started com atributos corretos")
    fun shouldRecordRedirectStarted() {
        // Given
        val partnerId = "partner-123"

        // When
        authMetrics.recordRedirectStarted(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("product")) == "auth" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("flow")) == "redirect"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica redirect.completed com success=true")
    fun shouldRecordRedirectCompleted() {
        // Given
        val partnerId = "partner-123"

        // When
        authMetrics.recordRedirectCompleted(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == true
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica redirect.failed com error_type")
    fun shouldRecordRedirectFailed() {
        // Given
        val partnerId = "partner-123"
        val errorType = "AuthenticationException"

        // When
        authMetrics.recordRedirectFailed(partnerId, errorType)

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
    @DisplayName("Deve emitir métrica redirect.duration em histograma")
    fun shouldRecordRedirectDuration() {
        // Given
        val partnerId = "partner-123"
        val durationMs = 150.0

        // When
        authMetrics.recordRedirectDuration(partnerId, durationMs)

        // Then
        verify {
            mockHistogram.record(durationMs, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("product")) == "auth"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica token.exchanged")
    fun shouldRecordTokenExchanged() {
        // Given
        val partnerId = "partner-123"

        // When
        authMetrics.recordTokenExchanged(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == true
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica opaque_token.generated com flow correto")
    fun shouldRecordOpaqueTokenGenerated() {
        // Given
        val partnerId = "partner-123"

        // When
        authMetrics.recordOpaqueTokenGenerated(partnerId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("flow")) == "opaque_token"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica impersonate.completed com restriction_type")
    fun shouldRecordImpersonateCompletedWithRestriction() {
        // Given
        val partnerId = "partner-123"
        val restrictionType = AuthRestrictionType.PARTNER_ACCOUNT

        // When
        authMetrics.recordImpersonateCompleted(partnerId, restrictionType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("auth_restriction_type")) == "PARTNER_ACCOUNT" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("flow")) == "impersonate"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica impersonate.failed com error_type")
    fun shouldRecordImpersonateFailed() {
        // Given
        val partnerId = "partner-123"
        val errorType = "ImpersonateException"

        // When
        authMetrics.recordImpersonateFailed(partnerId, errorType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("error_type")) == errorType &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == false
            })
        }
    }
}
