package io.arcotech.poc.metrics.service

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.infrastructure.metrics.domain.SenderMetrics
import org.springframework.stereotype.Service

/**
 * Serviço de simulação de envio de dados.
 * Simula os fluxos de sender emitindo métricas corretas.
 */
@Service
class SenderService(
    private val senderMetrics: SenderMetrics
) {

    /**
     * Simula recebimento de resultado de webhook de parceiro.
     */
    fun processWebhookResult(
        syncId: String,
        request: WebhookResultRequest
    ): WebhookResultResponse {
        val partnerId = request.partnerId

        // Sempre emite webhook received
        senderMetrics.recordWebhookReceived(partnerId)

        return when (request.status.uppercase()) {
            "SUCCESS" -> {
                senderMetrics.recordWebhookSuccess(partnerId)
                WebhookResultResponse(
                    success = true,
                    message = "Webhook result processed successfully for sync $syncId"
                )
            }
            "ERROR" -> {
                val errorType = request.errorMessage ?: "UnknownError"
                senderMetrics.recordWebhookError(partnerId, errorType)
                WebhookResultResponse(
                    success = true,
                    message = "Webhook error recorded for sync $syncId"
                )
            }
            else -> {
                senderMetrics.recordWebhookError(partnerId, "InvalidStatusException")
                WebhookResultResponse(
                    success = false,
                    message = "Invalid webhook status: ${request.status}"
                )
            }
        }
    }

    /**
     * Simula recebimento de notificação SNS.
     */
    fun processSnsNotification(
        request: SnsNotificationRequest,
        partnerId: String,
        simulateError: Boolean = false
    ): SnsNotificationResponse {
        // Sempre emite webhook received
        senderMetrics.recordWebhookReceived(partnerId)

        // Se for confirmação de subscription, apenas retorna sucesso
        if (request.type == "SubscriptionConfirmation") {
            senderMetrics.recordWebhookSuccess(partnerId)
            return SnsNotificationResponse(
                success = true,
                message = "Subscription confirmation received"
            )
        }

        return if (simulateError) {
            senderMetrics.recordWebhookError(partnerId, "SnsProcessingException")
            SnsNotificationResponse(
                success = false,
                message = "Failed to process SNS notification"
            )
        } else {
            senderMetrics.recordWebhookSuccess(partnerId)
            SnsNotificationResponse(
                success = true,
                message = "SNS notification processed successfully"
            )
        }
    }
}
