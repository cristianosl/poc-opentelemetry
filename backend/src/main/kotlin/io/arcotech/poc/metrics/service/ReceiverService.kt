package io.arcotech.poc.metrics.service

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.infrastructure.metrics.domain.ReceiverMetrics
import io.arcotech.poc.metrics.infrastructure.metrics.model.EntityType
import org.springframework.stereotype.Service
import java.util.UUID
import kotlin.random.Random

/**
 * Serviço de simulação de recebimento de entidades.
 * Simula os fluxos de receiver emitindo métricas corretas.
 */
@Service
class ReceiverService(
    private val receiverMetrics: ReceiverMetrics
) {

    /**
     * Simula recebimento de instituição.
     */
    fun receiveInstitution(
        externalInstitutionId: String,
        request: InstitutionRequest,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): InstitutionResponse {
        val partnerId = request.partnerId
        val startTime = System.currentTimeMillis()

        // Emitir received
        receiverMetrics.recordEntityReceived(partnerId, EntityType.INSTITUTION, externalInstitutionId)

        // Simular processamento
        simulateProcessing(simulateDelay)

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        receiverMetrics.recordProcessingDuration(partnerId, EntityType.INSTITUTION, duration, externalInstitutionId)

        return if (simulateError) {
            receiverMetrics.recordEntityFailed(partnerId, EntityType.INSTITUTION, "ValidationException", externalInstitutionId)
            InstitutionResponse(
                success = false,
                institutionId = null,
                message = "Failed to process institution: Validation error"
            )
        } else {
            receiverMetrics.recordEntityProcessed(partnerId, EntityType.INSTITUTION, externalInstitutionId)
            InstitutionResponse(
                success = true,
                institutionId = "inst_${UUID.randomUUID().toString().take(8)}",
                message = "Institution processed successfully"
            )
        }
    }

    /**
     * Simula recebimento de turmas.
     */
    fun receiveClassrooms(
        externalInstitutionId: String,
        request: ClassroomRequest,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): ClassroomResponse {
        val partnerId = request.partnerId
        val startTime = System.currentTimeMillis()

        receiverMetrics.recordEntityReceived(partnerId, EntityType.CLASSROOM, externalInstitutionId)

        simulateProcessing(simulateDelay)

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        receiverMetrics.recordProcessingDuration(partnerId, EntityType.CLASSROOM, duration, externalInstitutionId)

        return if (simulateError) {
            receiverMetrics.recordEntityFailed(partnerId, EntityType.CLASSROOM, "DuplicateException", externalInstitutionId)
            ClassroomResponse(
                success = false,
                classroomId = null,
                message = "Failed to process classroom: Duplicate entry"
            )
        } else {
            receiverMetrics.recordEntityProcessed(partnerId, EntityType.CLASSROOM, externalInstitutionId)
            ClassroomResponse(
                success = true,
                classroomId = "class_${UUID.randomUUID().toString().take(8)}",
                message = "Classroom processed successfully"
            )
        }
    }

    /**
     * Simula recebimento de usuários (genérico para todos os perfis).
     */
    fun receiveUser(
        externalInstitutionId: String,
        request: UserRequest,
        entityType: EntityType,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): UserResponse {
        val partnerId = request.partnerId
        val startTime = System.currentTimeMillis()

        receiverMetrics.recordEntityReceived(partnerId, entityType, externalInstitutionId)

        simulateProcessing(simulateDelay)

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        receiverMetrics.recordProcessingDuration(partnerId, entityType, duration, externalInstitutionId)

        return if (simulateError) {
            receiverMetrics.recordEntityFailed(partnerId, entityType, "InvalidDataException", externalInstitutionId)
            UserResponse(
                success = false,
                userId = null,
                message = "Failed to process ${entityType.value.lowercase()}: Invalid data"
            )
        } else {
            receiverMetrics.recordEntityProcessed(partnerId, entityType, externalInstitutionId)
            UserResponse(
                success = true,
                userId = "${entityType.value.lowercase()}_${UUID.randomUUID().toString().take(8)}",
                message = "${entityType.value} processed successfully"
            )
        }
    }

    /**
     * Simula recebimento de licenças.
     */
    fun receiveLicense(
        crmId: String,
        request: LicenseRequest,
        simulateError: Boolean = false,
        simulateDelay: Long = 0
    ): LicenseResponse {
        val partnerId = request.partnerId
        val startTime = System.currentTimeMillis()

        receiverMetrics.recordEntityReceived(partnerId, EntityType.LICENSE, crmId)

        simulateProcessing(simulateDelay)

        val duration = (System.currentTimeMillis() - startTime).toDouble()
        receiverMetrics.recordProcessingDuration(partnerId, EntityType.LICENSE, duration, crmId)

        return if (simulateError) {
            receiverMetrics.recordEntityFailed(partnerId, EntityType.LICENSE, "LicenseQuotaException", crmId)
            LicenseResponse(
                success = false,
                licenseId = null,
                message = "Failed to process license: Quota exceeded"
            )
        } else {
            receiverMetrics.recordEntityProcessed(partnerId, EntityType.LICENSE, crmId)
            LicenseResponse(
                success = true,
                licenseId = "lic_${UUID.randomUUID().toString().take(8)}",
                message = "License processed successfully"
            )
        }
    }

    /**
     * Simula deleção de entidade.
     */
    fun deleteEntity(
        partnerId: String,
        entityType: EntityType,
        institutionId: String? = null
    ) {
        receiverMetrics.recordEntityDeleted(partnerId, entityType, institutionId)
    }

    private fun simulateProcessing(simulateDelay: Long) {
        if (simulateDelay > 0) {
            Thread.sleep(simulateDelay)
        } else {
            Thread.sleep(Random.nextLong(20, 100))
        }
    }
}
