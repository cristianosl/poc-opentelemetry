package io.arcotech.poc.metrics.infrastructure.metrics.model

/**
 * Definição das métricas de produto seguindo a convenção:
 * integration.<product>.<resource>.<action>
 */
object ProductMetricType {

    // ==================== AUTH METRICS ====================

    object Auth {
        // Counters
        const val REDIRECT_STARTED = "integration.auth.redirect.started"
        const val REDIRECT_COMPLETED = "integration.auth.redirect.completed"
        const val REDIRECT_FAILED = "integration.auth.redirect.failed"
        const val TOKEN_EXCHANGED = "integration.auth.token.exchanged"
        const val TOKEN_VALIDATED = "integration.auth.token.validated"
        const val TOKEN_VALIDATION_FAILED = "integration.auth.token.validation_failed"
        const val OPAQUE_TOKEN_GENERATED = "integration.auth.opaque_token.generated"
        const val OPAQUE_TOKEN_VALIDATED = "integration.auth.opaque_token.validated"
        const val IMPERSONATE_COMPLETED = "integration.auth.impersonate.completed"
        const val IMPERSONATE_FAILED = "integration.auth.impersonate.failed"

        // Histograms
        const val REDIRECT_DURATION = "integration.auth.redirect.duration"
    }

    // ==================== RECEIVER METRICS ====================

    object Receiver {
        // Counters
        const val ENTITY_RECEIVED = "integration.receiver.entity.received"
        const val ENTITY_PROCESSED = "integration.receiver.entity.processed"
        const val ENTITY_FAILED = "integration.receiver.entity.failed"
        const val ENTITY_DELETED = "integration.receiver.entity.deleted"

        // Histograms
        const val PROCESSING_DURATION = "integration.receiver.processing.duration"
    }

    // ==================== SENDER METRICS ====================

    object Sender {
        // Counters
        const val WEBHOOK_RECEIVED = "integration.sender.webhook.received"
        const val WEBHOOK_SUCCESS = "integration.sender.webhook.success"
        const val WEBHOOK_ERROR = "integration.sender.webhook.error"
    }
}
