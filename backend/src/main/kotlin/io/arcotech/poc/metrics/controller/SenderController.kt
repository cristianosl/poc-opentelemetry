package io.arcotech.poc.metrics.controller

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.service.SenderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller para endpoints de envio de dados.
 *
 * Endpoints:
 * - POST /api/v1/integrations/results/{syncId} - Webhook de confirmação de parceiro
 * - POST /api/v1/webhooks - Notificações AWS SNS
 */
@RestController
@RequestMapping("/api/v1")
class SenderController(
    private val senderService: SenderService
) {

    /**
     * POST /api/v1/integrations/results/{syncId}
     * Simula webhook de confirmação de parceiro.
     *
     * Métricas: sender.webhook.received, sender.webhook.success, sender.webhook.error
     */
    @PostMapping("/integrations/results/{syncId}")
    fun receiveWebhookResult(
        @PathVariable syncId: String,
        @RequestBody request: WebhookResultRequest
    ): ResponseEntity<WebhookResultResponse> {
        val response = senderService.processWebhookResult(syncId, request)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/webhooks
     * Simula recebimento de notificação AWS SNS.
     *
     * Métricas: sender.webhook.received, sender.webhook.success, sender.webhook.error
     */
    @PostMapping("/webhooks")
    fun receiveSnsNotification(
        @RequestBody request: SnsNotificationRequest,
        @RequestParam(name = "partner_id", defaultValue = "default-partner") partnerId: String,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean
    ): ResponseEntity<SnsNotificationResponse> {
        val response = senderService.processSnsNotification(request, partnerId, simulateError)
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }
}
