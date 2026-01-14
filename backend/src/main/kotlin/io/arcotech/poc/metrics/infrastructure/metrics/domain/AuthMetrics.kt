package io.arcotech.poc.metrics.infrastructure.metrics.domain

import io.arcotech.poc.metrics.infrastructure.metrics.ProductMetricsService
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthFlow
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthRestrictionType
import io.arcotech.poc.metrics.infrastructure.metrics.model.Product
import io.arcotech.poc.metrics.infrastructure.metrics.model.ProductMetricType
import org.springframework.stereotype.Component

/**
 * Métricas de autenticação (Auth).
 *
 * Counters:
 * - integration.auth.redirect.started
 * - integration.auth.redirect.completed
 * - integration.auth.redirect.failed
 * - integration.auth.token.exchanged
 * - integration.auth.token.validated
 * - integration.auth.token.validation_failed
 * - integration.auth.opaque_token.generated
 * - integration.auth.opaque_token.validated
 * - integration.auth.impersonate.completed
 * - integration.auth.impersonate.failed
 *
 * Histogramas:
 * - integration.auth.redirect.duration
 */
@Component
class AuthMetrics(
    private val metricsService: ProductMetricsService
) {

    // ==================== REDIRECT ====================

    fun recordRedirectStarted(partnerId: String, flow: AuthFlow = AuthFlow.REDIRECT) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = flow.value)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.REDIRECT_STARTED,
            attributes,
            "Número de fluxos de redirect iniciados"
        )
    }

    fun recordRedirectCompleted(partnerId: String, flow: AuthFlow = AuthFlow.REDIRECT) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = flow.value, success = true)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.REDIRECT_COMPLETED,
            attributes,
            "Número de redirects concluídos com sucesso"
        )
    }

    fun recordRedirectFailed(partnerId: String, errorType: String, flow: AuthFlow = AuthFlow.REDIRECT) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = flow.value, success = false, errorType = errorType)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.REDIRECT_FAILED,
            attributes,
            "Número de redirects que falharam"
        )
    }

    fun recordRedirectDuration(partnerId: String, durationMs: Double, flow: AuthFlow = AuthFlow.REDIRECT) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = flow.value)
            .build()
        metricsService.recordHistogram(
            ProductMetricType.Auth.REDIRECT_DURATION,
            durationMs,
            attributes,
            "Tempo de processamento do redirect",
            "ms"
        )
    }

    // ==================== TOKEN ====================

    fun recordTokenExchanged(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, success = true)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.TOKEN_EXCHANGED,
            attributes,
            "Número de tokens trocados com sucesso"
        )
    }

    fun recordTokenValidated(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, success = true)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.TOKEN_VALIDATED,
            attributes,
            "Número de tokens validados"
        )
    }

    fun recordTokenValidationFailed(partnerId: String, errorType: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, success = false, errorType = errorType)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.TOKEN_VALIDATION_FAILED,
            attributes,
            "Número de validações de token que falharam"
        )
    }

    // ==================== OPAQUE TOKEN ====================

    fun recordOpaqueTokenGenerated(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = AuthFlow.OPAQUE_TOKEN.value)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.OPAQUE_TOKEN_GENERATED,
            attributes,
            "Número de opaque tokens gerados"
        )
    }

    fun recordOpaqueTokenValidated(partnerId: String) {
        val attributes = metricsService.attributesBuilder()
            .copy(product = Product.AUTH.value, partnerId = partnerId, flow = AuthFlow.OPAQUE_TOKEN.value, success = true)
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.OPAQUE_TOKEN_VALIDATED,
            attributes,
            "Número de opaque tokens validados"
        )
    }

    // ==================== IMPERSONATE ====================

    fun recordImpersonateCompleted(partnerId: String, restrictionType: AuthRestrictionType? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.AUTH.value,
                partnerId = partnerId,
                flow = AuthFlow.IMPERSONATE.value,
                success = true,
                authRestrictionType = restrictionType?.value
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.IMPERSONATE_COMPLETED,
            attributes,
            "Número de impersonates concluídos"
        )
    }

    fun recordImpersonateFailed(partnerId: String, errorType: String, restrictionType: AuthRestrictionType? = null) {
        val attributes = metricsService.attributesBuilder()
            .copy(
                product = Product.AUTH.value,
                partnerId = partnerId,
                flow = AuthFlow.IMPERSONATE.value,
                success = false,
                errorType = errorType,
                authRestrictionType = restrictionType?.value
            )
            .build()
        metricsService.incrementCounter(
            ProductMetricType.Auth.IMPERSONATE_FAILED,
            attributes,
            "Número de impersonates que falharam"
        )
    }
}
