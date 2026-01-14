package io.arcotech.poc.metrics.controller

import io.arcotech.poc.metrics.dto.*
import io.arcotech.poc.metrics.infrastructure.metrics.model.EntityType
import io.arcotech.poc.metrics.service.ReceiverService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller para endpoints de recebimento de entidades.
 *
 * Endpoints:
 * - POST /api/v1/institutions/{externalInstitutionId} - Instituições
 * - POST /api/v1/institutions/{externalInstitutionId}/classrooms - Turmas
 * - POST /api/v1/institutions/{externalInstitutionId}/admins - Admins
 * - POST /api/v1/institutions/{externalInstitutionId}/coordinators - Coordenadores
 * - POST /api/v1/institutions/{externalInstitutionId}/teachers - Professores
 * - POST /api/v1/institutions/{externalInstitutionId}/students - Alunos
 * - POST /api/v1/institutions/{crmId}/licenses - Licenças
 */
@RestController
@RequestMapping("/api/v1/institutions")
class ReceiverController(
    private val receiverService: ReceiverService
) {

    /**
     * POST /api/v1/institutions/{externalInstitutionId}
     * Simula recebimento de instituição.
     *
     * Métricas: receiver.entity.received (entity_type=Institution),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}")
    fun receiveInstitution(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: InstitutionRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<InstitutionResponse> {
        val response = receiverService.receiveInstitution(
            externalInstitutionId, request, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{externalInstitutionId}/classrooms
     * Simula recebimento de turmas.
     *
     * Métricas: receiver.entity.received (entity_type=Classroom),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}/classrooms")
    fun receiveClassrooms(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: ClassroomRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<ClassroomResponse> {
        val response = receiverService.receiveClassrooms(
            externalInstitutionId, request, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{externalInstitutionId}/admins
     * Simula recebimento de administradores.
     *
     * Métricas: receiver.entity.received (entity_type=Admin),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}/admins")
    fun receiveAdmins(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: UserRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<UserResponse> {
        val response = receiverService.receiveUser(
            externalInstitutionId, request, EntityType.ADMIN, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{externalInstitutionId}/coordinators
     * Simula recebimento de coordenadores.
     *
     * Métricas: receiver.entity.received (entity_type=Coordinator),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}/coordinators")
    fun receiveCoordinators(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: UserRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<UserResponse> {
        val response = receiverService.receiveUser(
            externalInstitutionId, request, EntityType.COORDINATOR, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{externalInstitutionId}/teachers
     * Simula recebimento de professores.
     *
     * Métricas: receiver.entity.received (entity_type=Teacher),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}/teachers")
    fun receiveTeachers(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: UserRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<UserResponse> {
        val response = receiverService.receiveUser(
            externalInstitutionId, request, EntityType.TEACHER, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{externalInstitutionId}/students
     * Simula recebimento de alunos.
     *
     * Métricas: receiver.entity.received (entity_type=Student),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{externalInstitutionId}/students")
    fun receiveStudents(
        @PathVariable externalInstitutionId: String,
        @RequestBody request: UserRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<UserResponse> {
        val response = receiverService.receiveUser(
            externalInstitutionId, request, EntityType.STUDENT, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }

    /**
     * POST /api/v1/institutions/{crmId}/licenses
     * Simula recebimento de licenças.
     *
     * Métricas: receiver.entity.received (entity_type=License),
     *           receiver.entity.processed, receiver.entity.failed,
     *           receiver.processing.duration
     */
    @PostMapping("/{crmId}/licenses")
    fun receiveLicenses(
        @PathVariable crmId: String,
        @RequestBody request: LicenseRequest,
        @RequestParam(name = "simulate_error", defaultValue = "false") simulateError: Boolean,
        @RequestParam(name = "simulate_delay", defaultValue = "0") simulateDelay: Long
    ): ResponseEntity<LicenseResponse> {
        val response = receiverService.receiveLicense(
            crmId, request, simulateError, simulateDelay
        )
        return if (response.success) {
            ResponseEntity.ok(response)
        } else {
            ResponseEntity.status(400).body(response)
        }
    }
}
