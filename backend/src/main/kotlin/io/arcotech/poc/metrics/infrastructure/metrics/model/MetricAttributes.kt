package io.arcotech.poc.metrics.infrastructure.metrics.model

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes

/**
 * Atributos padronizados para métricas de produto.
 */
object MetricAttributes {
    // Atributos obrigatórios
    val SERVICE: AttributeKey<String> = AttributeKey.stringKey("service")
    val PRODUCT: AttributeKey<String> = AttributeKey.stringKey("product")
    val ENVIRONMENT: AttributeKey<String> = AttributeKey.stringKey("environment")
    val PARTNER_ID: AttributeKey<String> = AttributeKey.stringKey("partner_id")

    // Atributos opcionais
    val FLOW: AttributeKey<String> = AttributeKey.stringKey("flow")
    val ENTITY_TYPE: AttributeKey<String> = AttributeKey.stringKey("entity_type")
    val INSTITUTION_ID: AttributeKey<String> = AttributeKey.stringKey("institution_id")
    val ERROR_TYPE: AttributeKey<String> = AttributeKey.stringKey("error_type")
    val SUCCESS: AttributeKey<Boolean> = AttributeKey.booleanKey("success")
    val AUTH_RESTRICTION_TYPE: AttributeKey<String> = AttributeKey.stringKey("auth_restriction_type")
}

/**
 * Builder para criar atributos de métricas.
 */
data class MetricAttributesBuilder(
    var service: String = "poc-metrics-backend",
    var product: String = "",
    var environment: String = "local",
    var partnerId: String = "",
    var flow: String? = null,
    var entityType: String? = null,
    var institutionId: String? = null,
    var errorType: String? = null,
    var success: Boolean? = null,
    var authRestrictionType: String? = null
) {
    fun build(): Attributes {
        val builder = Attributes.builder()
            .put(MetricAttributes.SERVICE, service)
            .put(MetricAttributes.PRODUCT, product)
            .put(MetricAttributes.ENVIRONMENT, environment)
            .put(MetricAttributes.PARTNER_ID, partnerId)

        flow?.let { builder.put(MetricAttributes.FLOW, it) }
        entityType?.let { builder.put(MetricAttributes.ENTITY_TYPE, it) }
        institutionId?.let { builder.put(MetricAttributes.INSTITUTION_ID, it) }
        errorType?.let { builder.put(MetricAttributes.ERROR_TYPE, it) }
        success?.let { builder.put(MetricAttributes.SUCCESS, it) }
        authRestrictionType?.let { builder.put(MetricAttributes.AUTH_RESTRICTION_TYPE, it) }

        return builder.build()
    }
}

/**
 * Produtos disponíveis para métricas.
 */
enum class Product(val value: String) {
    AUTH("auth"),
    RECEIVER("receiver"),
    SENDER("sender")
}

/**
 * Tipos de fluxo de autenticação.
 */
enum class AuthFlow(val value: String) {
    REDIRECT("redirect"),
    OPAQUE_TOKEN("opaque_token"),
    IMPERSONATE("impersonate")
}

/**
 * Tipos de entidade do receiver.
 */
enum class EntityType(val value: String) {
    INSTITUTION("Institution"),
    CLASSROOM("Classroom"),
    ADMIN("Admin"),
    COORDINATOR("Coordinator"),
    TEACHER("Teacher"),
    STUDENT("Student"),
    LICENSE("License")
}

/**
 * Tipos de restrição de autenticação.
 */
enum class AuthRestrictionType(val value: String) {
    BU("BU"),
    PARTNER_ACCOUNT("PARTNER_ACCOUNT"),
    PARTNER_INSTITUTION("PARTNER_INSTITUTION")
}
