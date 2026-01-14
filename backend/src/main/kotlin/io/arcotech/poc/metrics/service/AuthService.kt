package io.arcotech.poc.metrics.service

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.infrastructure.metrics.domain.AuthMetrics
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthFlow
import io.arcotech.poc.metrics.infrastructure.metrics.model.AuthRestrictionType
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

/**
 * Serviço de simulação de autenticação.
 * Simula os fluxos de auth emitindo métricas corretas.
 */
@Service
class AuthService(
    private val authMetrics: AuthMetrics
) {

    /**
     * Simula autenticação V1 via username.
     * Fluxo: redirect → token exchange → impersonate
     */
    fun authenticateV1(
        request: AuthV1Request,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): AuthV1Response {
        val partnerId = request.partnerId
        val startTime = System.currentTimeMillis()

        // Emitir redirect started
        authMetrics.recordRedirectStarted(partnerId, AuthFlow.REDIRECT)

        // Simular delay se solicitado
        if (simulateDelay > 0) {
            Thread.sleep(simulateDelay)
        } else {
            // Delay aleatório entre 50-200ms
            Thread.sleep(Random.nextLong(50, 200))
        }

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        authMetrics.recordRedirectDuration(partnerId, duration, AuthFlow.REDIRECT)

        return if (simulateError) {
            // Simular erro
            authMetrics.recordRedirectFailed(partnerId, "AuthenticationException", AuthFlow.REDIRECT)
            authMetrics.recordImpersonateFailed(partnerId, "ImpersonateException")
            AuthV1Response(
                success = false,
                redirectUrl = null,
                accessToken = null,
                refreshToken = null,
                message = "Authentication failed: Invalid credentials"
            )
        } else {
            // Simular sucesso
            authMetrics.recordRedirectCompleted(partnerId, AuthFlow.REDIRECT)
            authMetrics.recordTokenExchanged(partnerId)
            authMetrics.recordImpersonateCompleted(partnerId)
            AuthV1Response(
                success = true,
                redirectUrl = "https://app.example.com/dashboard",
                accessToken = "mock_access_token_${UUID.randomUUID()}",
                refreshToken = "mock_refresh_token_${UUID.randomUUID()}",
                message = "Authentication successful"
            )
        }
    }

    /**
     * Simula autenticação V2 com estratégias de restrição.
     * Fluxo: redirect → opaque token → token exchange → impersonate
     */
    fun authenticateV2(
        request: AuthV2Request,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): AuthV2Response {
        val partnerId = request.partnerId
        val restrictionType = request.restrictionType?.let { AuthRestrictionType.valueOf(it) }
        val startTime = System.currentTimeMillis()

        // Emitir redirect started
        authMetrics.recordRedirectStarted(partnerId, AuthFlow.REDIRECT)

        // Simular delay
        if (simulateDelay > 0) {
            Thread.sleep(simulateDelay)
        } else {
            Thread.sleep(Random.nextLong(50, 200))
        }

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        authMetrics.recordRedirectDuration(partnerId, duration, AuthFlow.REDIRECT)

        return if (simulateError) {
            authMetrics.recordRedirectFailed(partnerId, "AuthenticationException", AuthFlow.REDIRECT)
            authMetrics.recordImpersonateFailed(partnerId, "ImpersonateException", restrictionType)
            AuthV2Response(
                success = false,
                opaqueToken = null,
                redirectUrl = null,
                message = "Authentication failed: Access denied"
            )
        } else {
            authMetrics.recordRedirectCompleted(partnerId, AuthFlow.REDIRECT)
            authMetrics.recordOpaqueTokenGenerated(partnerId)
            authMetrics.recordTokenExchanged(partnerId)
            authMetrics.recordImpersonateCompleted(partnerId, restrictionType)
            AuthV2Response(
                success = true,
                opaqueToken = "opaque_${UUID.randomUUID()}",
                redirectUrl = "https://app.example.com/callback",
                message = "Authentication successful"
            )
        }
    }

    /**
     * Simula troca de opaque token por tokens de acesso.
     */
    fun exchangeToken(
        request: TokenExchangeRequest,
        simulateError: Boolean = false
    ): TokenExchangeResponse {
        val partnerId = request.partnerId

        return if (simulateError) {
            authMetrics.recordTokenValidationFailed(partnerId, "InvalidTokenException")
            TokenExchangeResponse(
                success = false,
                accessToken = null,
                refreshToken = null,
                expiresIn = null,
                message = "Token exchange failed: Invalid opaque token"
            )
        } else {
            authMetrics.recordOpaqueTokenValidated(partnerId)
            authMetrics.recordTokenValidated(partnerId)
            TokenExchangeResponse(
                success = true,
                accessToken = "access_${UUID.randomUUID()}",
                refreshToken = "refresh_${UUID.randomUUID()}",
                expiresIn = 3600,
                message = "Token exchange successful"
            )
        }
    }

    /**
     * Simula validação de JWT e geração de opaque token (redirect endpoint).
     */
    fun validateAndGenerateOpaqueToken(
        partnerId: String,
        simulateError: Boolean = false
    ): AuthV2Response {
        return if (simulateError) {
            authMetrics.recordTokenValidationFailed(partnerId, "InvalidJwtException")
            AuthV2Response(
                success = false,
                opaqueToken = null,
                redirectUrl = null,
                message = "JWT validation failed"
            )
        } else {
            authMetrics.recordTokenValidated(partnerId)
            authMetrics.recordOpaqueTokenGenerated(partnerId)
            AuthV2Response(
                success = true,
                opaqueToken = "opaque_${UUID.randomUUID()}",
                redirectUrl = "https://app.example.com/session",
                message = "Opaque token generated"
            )
        }
    }

    /**
     * Simula validação de sessão via opaque token.
     */
    fun validateSession(
        request: SessionValidationRequest,
        simulateError: Boolean = false
    ): SessionValidationResponse {
        val partnerId = request.partnerId

        return if (simulateError) {
            authMetrics.recordTokenValidationFailed(partnerId, "SessionExpiredException")
            SessionValidationResponse(
                valid = false,
                userId = null,
                message = "Session validation failed: Session expired"
            )
        } else {
            authMetrics.recordOpaqueTokenValidated(partnerId)
            SessionValidationResponse(
                valid = true,
                userId = "user_${UUID.randomUUID().toString().take(8)}",
                message = "Session is valid"
            )
        }
    }
}
