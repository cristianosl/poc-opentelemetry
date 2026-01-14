package io.arcotech.poc.metrics.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ==================== AUTH V1 ====================

data class AuthV1Request(
    val username: String,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class AuthV1Response(
    val success: Boolean,
    @JsonProperty("redirect_url")
    val redirectUrl: String?,
    @JsonProperty("access_token")
    val accessToken: String?,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    val message: String?
)

// ==================== AUTH V2 ====================

data class AuthV2Request(
    val username: String,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner",
    @JsonProperty("restriction_type")
    val restrictionType: String? = null // BU, PARTNER_ACCOUNT, PARTNER_INSTITUTION
)

data class AuthV2Response(
    val success: Boolean,
    @JsonProperty("opaque_token")
    val opaqueToken: String?,
    @JsonProperty("redirect_url")
    val redirectUrl: String?,
    val message: String?
)

// ==================== TOKEN EXCHANGE ====================

data class TokenExchangeRequest(
    @JsonProperty("opaque_token")
    val opaqueToken: String,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class TokenExchangeResponse(
    val success: Boolean,
    @JsonProperty("access_token")
    val accessToken: String?,
    @JsonProperty("refresh_token")
    val refreshToken: String?,
    @JsonProperty("expires_in")
    val expiresIn: Long?,
    val message: String?
)

// ==================== SESSION VALIDATION ====================

data class SessionValidationRequest(
    @JsonProperty("opaque_token")
    val opaqueToken: String,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class SessionValidationResponse(
    val valid: Boolean,
    @JsonProperty("user_id")
    val userId: String?,
    val message: String?
)
