package io.arcotech.poc.metrics.dto

import com.fasterxml.jackson.annotation.JsonProperty

// ==================== INSTITUTION ====================

data class InstitutionRequest(
    val name: String,
    val code: String? = null,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class InstitutionResponse(
    val success: Boolean,
    @JsonProperty("institution_id")
    val institutionId: String?,
    val message: String?
)

// ==================== CLASSROOM ====================

data class ClassroomRequest(
    val name: String,
    val grade: String? = null,
    val shift: String? = null,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class ClassroomResponse(
    val success: Boolean,
    @JsonProperty("classroom_id")
    val classroomId: String?,
    val message: String?
)

// ==================== USER (Admin, Coordinator, Teacher, Student) ====================

data class UserRequest(
    val name: String,
    val email: String? = null,
    @JsonProperty("external_id")
    val externalId: String? = null,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class UserResponse(
    val success: Boolean,
    @JsonProperty("user_id")
    val userId: String?,
    val message: String?
)

// ==================== LICENSE ====================

data class LicenseRequest(
    @JsonProperty("product_code")
    val productCode: String,
    val quantity: Int,
    @JsonProperty("start_date")
    val startDate: String? = null,
    @JsonProperty("end_date")
    val endDate: String? = null,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class LicenseResponse(
    val success: Boolean,
    @JsonProperty("license_id")
    val licenseId: String?,
    val message: String?
)

// ==================== BATCH REQUEST ====================

data class BatchRequest<T>(
    val items: List<T>,
    @JsonProperty("partner_id")
    val partnerId: String = "default-partner"
)

data class BatchResponse(
    val success: Boolean,
    @JsonProperty("processed_count")
    val processedCount: Int,
    @JsonProperty("failed_count")
    val failedCount: Int,
    val message: String?
)
