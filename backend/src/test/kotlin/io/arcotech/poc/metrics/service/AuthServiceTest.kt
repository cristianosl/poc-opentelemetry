package io.arcotech.poc.metrics.service

import io.arcotech.poc.metrics.dto.AuthV1Request
import io.arcotech.poc.metrics.dto.AuthV2Request
import io.arcotech.poc.metrics.dto.TokenExchangeRequest
import io.arcotech.poc.metrics.dto.SessionValidationRequest
import io.arcotech.poc.metrics.infrastructure.metrics.domain.AuthMetrics
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*

/**
 * Testes unitários para AuthService.
 * Valida que o serviço emite as métricas corretas em cada cenário.
 */
class AuthServiceTest {

    private lateinit var authMetrics: AuthMetrics
    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authMetrics = mockk(relaxed = true)
        authService = AuthService(authMetrics)
    }

    @Test
    @DisplayName("authenticateV1 deve emitir métricas de sucesso")
    fun authenticateV1ShouldEmitSuccessMetrics() {
        // Given
        val request = AuthV1Request(username = "testuser", partnerId = "partner-123")

        // When
        val response = authService.authenticateV1(request, simulateError = false, simulateDelay = 10)

        // Then
        assertTrue(response.success)
        assertNotNull(response.accessToken)
        assertNotNull(response.refreshToken)

        verifyOrder {
            authMetrics.recordRedirectStarted("partner-123", any())
            authMetrics.recordRedirectDuration("partner-123", any(), any())
            authMetrics.recordRedirectCompleted("partner-123", any())
            authMetrics.recordTokenExchanged("partner-123")
            authMetrics.recordImpersonateCompleted("partner-123", null)
        }
    }

    @Test
    @DisplayName("authenticateV1 deve emitir métricas de erro quando simulate_error=true")
    fun authenticateV1ShouldEmitErrorMetrics() {
        // Given
        val request = AuthV1Request(username = "testuser", partnerId = "partner-123")

        // When
        val response = authService.authenticateV1(request, simulateError = true, simulateDelay = 10)

        // Then
        assertFalse(response.success)
        assertNull(response.accessToken)

        verify {
            authMetrics.recordRedirectStarted("partner-123", any())
            authMetrics.recordRedirectDuration("partner-123", any(), any())
            authMetrics.recordRedirectFailed("partner-123", "AuthenticationException", any())
            authMetrics.recordImpersonateFailed("partner-123", "ImpersonateException", null)
        }
    }

    @Test
    @DisplayName("authenticateV2 deve emitir métrica opaque_token.generated")
    fun authenticateV2ShouldEmitOpaqueTokenMetric() {
        // Given
        val request = AuthV2Request(
            username = "testuser",
            partnerId = "partner-123",
            restrictionType = "PARTNER_ACCOUNT"
        )

        // When
        val response = authService.authenticateV2(request, simulateError = false, simulateDelay = 10)

        // Then
        assertTrue(response.success)
        assertNotNull(response.opaqueToken)

        verify {
            authMetrics.recordOpaqueTokenGenerated("partner-123")
        }
    }

    @Test
    @DisplayName("exchangeToken deve emitir métricas de validação")
    fun exchangeTokenShouldEmitValidationMetrics() {
        // Given
        val request = TokenExchangeRequest(opaqueToken = "opaque-token", partnerId = "partner-123")

        // When
        val response = authService.exchangeToken(request, simulateError = false)

        // Then
        assertTrue(response.success)
        assertNotNull(response.accessToken)

        verify {
            authMetrics.recordOpaqueTokenValidated("partner-123")
            authMetrics.recordTokenValidated("partner-123")
        }
    }

    @Test
    @DisplayName("exchangeToken deve emitir métrica de falha quando simulate_error=true")
    fun exchangeTokenShouldEmitFailureMetric() {
        // Given
        val request = TokenExchangeRequest(opaqueToken = "invalid-token", partnerId = "partner-123")

        // When
        val response = authService.exchangeToken(request, simulateError = true)

        // Then
        assertFalse(response.success)

        verify {
            authMetrics.recordTokenValidationFailed("partner-123", "InvalidTokenException")
        }
    }

    @Test
    @DisplayName("validateSession deve emitir métrica opaque_token.validated")
    fun validateSessionShouldEmitOpaqueTokenValidated() {
        // Given
        val request = SessionValidationRequest(opaqueToken = "opaque-token", partnerId = "partner-123")

        // When
        val response = authService.validateSession(request, simulateError = false)

        // Then
        assertTrue(response.valid)
        assertNotNull(response.userId)

        verify {
            authMetrics.recordOpaqueTokenValidated("partner-123")
        }
    }
}
