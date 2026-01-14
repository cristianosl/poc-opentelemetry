package io.arcotech.poc.metrics.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ==================== WEBHOOK RESULT ====================

data class WebhookResultRequest(
    val status: String, // SUCCESS, ERROR
    @JsonProperty("error_message")
    val errorMessage: String? = null,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner",
    @JsonProperty("processed_count")
    val processedCount: Int? = null
)

data class WebhookResultResponse(
    val success: Boolean,
    val message: String?
)

// ==================== SNS NOTIFICATION ====================

data class SnsNotificationRequest(
    @JsonProperty("Type")
    val type: String, // Notification, SubscriptionConfirmation
    @JsonProperty("Message")
    val message: String,
    @JsonProperty("TopicArn")
    val topicArn: String? = null,
    @JsonProperty("SubscribeURL")
    val subscribeUrl: String? = null
)

data class SnsNotificationResponse(
    val success: Boolean,
    val message: String?
)
