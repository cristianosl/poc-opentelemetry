package io.arcotech.poc.metrics.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller para endpoints utilitários.
 *
 * Endpoints:
 * - GET /api/health - Health check
 */
@RestController
@RequestMapping("/api")
class HealthController {

    data class HealthResponse(
        val status: String,
        val service: String,
        val version: String
    )

    /**
     * GET /api/health
     * Health check endpoint.
     */
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
            HealthResponse(
                status = "UP",
                service = "poc-metrics-backend",
                version = "0.0.1-SNAPSHOT"
            )
        )
    }
}
