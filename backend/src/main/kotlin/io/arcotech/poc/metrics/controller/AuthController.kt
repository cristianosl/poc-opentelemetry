package io.arcotech.poc.metrics.controller

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller para endpoints de autenticação.
 *
 * Endpoints:
 * - POST /api/v1/auth - Login SSO v1
 * - POST /api/v2/auth - Login SSO v2 com restrições
 * - POST /api/v1/auth/token - Troca de opaque token
 * - GET /api/v1/auth/redirect - Validação JWT e geração de opaque token
 * - POST /api/v1/auth/session - Validação de sessão
 */
@RestController
@RequestMapping("/api")
class AuthController(
    private val authService: AuthService
) {

    /**
     * POST /api/v1/auth
     * Simula autenticação via username (dados mockados).
     *
     * Métricas: auth.redirect.started, auth.redirect.completed, auth.redirect.failed,
     *           auth.redirect.duration, auth.token.exchanged, auth.impersonate.completed,
     *           auth.impersonate.failed
     */
    @PostMapping("/v1/auth")
    fun authenticateV1(
        @RequestBody request: AuthV1Request,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<AuthV1Response> {
        val response = authService.authenticateV1(request, simulateError, simulateDelay)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(401).body(response)
        }
    }

    /**
     * POST /api/v2/auth
     * Simula autenticação com estratégias de restrição.
     *
     * Métricas: auth.redirect.started, auth.redirect.completed, auth.redirect.failed,
     *           auth.redirect.duration, auth.opaque_token.generated, auth.token.exchanged,
     *           auth.impersonate.completed, auth.impersonate.failed
     */
    @PostMapping("/v2/auth")
    fun authenticateV2(
        @RequestBody request: AuthV2Request,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<AuthV2Response> {
        val response = authService.authenticateV2(request, simulateError, simulateDelay)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(401).body(response)
        }
    }

    /**
     * POST /api/v1/auth/token
     * Simula troca de opaque token por tokens de acesso.
     *
     * Métricas: auth.opaque_token.validated, auth.token.validation_failed, auth.token.validated
     */
    @PostMapping("/v1/auth/token")
    fun exchangeToken(
        @RequestBody request: TokenExchangeRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean
    ): ResponseEntity<TokenExchangeResponse> {
        val response = authService.exchangeToken(request, simulateError)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * GET /api/v1/auth/redirect
     * Simula validação de JWT e geração de opaque token.
     *
     * Métricas: auth.opaque_token.generated, auth.token.validated, auth.token.validation_failed
     */
    @GetMapping("/v1/auth/redirect")
    fun validateAndRedirect(
        @RequestParam(name = "partner_id", defaultValue = "default-partner") partnerId: String,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean
    ): ResponseEntity<AuthV2Response> {
        val response = authService.validateAndGenerateOpaqueToken(partnerId, simulateError)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/auth/session
     * Simula validação de sessão via opaque token.
     *
     * Métricas: auth.opaque_token.validated, auth.token.validation_failed
     */
    @PostMapping("/v1/auth/session")
    fun validateSession(
        @RequestBody request: SessionValidationRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean
    ): ResponseEntity<SessionValidationResponse> {
        val response = authService.validateSession(request, simulateError)
        return if (response.valid) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(401).body(response)
        }
    }
}
