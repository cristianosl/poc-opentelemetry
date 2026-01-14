package io.arcotech.poc.metrics.infrastructure.metrics

import io.arcotech.poc.metrics.infrastructure.metrics.domain.ReceiverMetrics
import io.arcotech.poc.metrics.infrastructure.metrics.model.EntityType
import io.mockk.*
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.DoubleHistogram
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.Meter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

/**
 * Testes unitários para ReceiverMetrics.
 * Valida que as métricas são emitidas corretamente com os atributos esperados.
 */
class ReceiverMetricsTest {

    private lateinit var meter: Meter
    private lateinit var metricsService: ProductMetricsService
    private lateinit var receiverMetrics: ReceiverMetrics
    private lateinit var mockCounter: LongCounter
    private lateinit var mockHistogram: DoubleHistogram

    @BeforeEach
    fun setup() {
        meter = mockk(relaxed = true)
        mockCounter = mockk(relaxed = true)
        mockHistogram = mockk(relaxed = true)

        every { meter.counterBuilder(any()).setDescription(any()).setUnit(any()).build() } returns mockCounter
        every { meter.histogramBuilder(any()).setDescription(any()).setUnit(any()).build() } returns mockHistogram

        metricsService = ProductMetricsService(meter, "poc-metrics-backend", "local")
        receiverMetrics = ReceiverMetrics(metricsService)
    }

    @Test
    @DisplayName("Deve emitir métrica entity.received com entity_type correto")
    fun shouldRecordEntityReceived() {
        // Given
        val partnerId = "partner-123"
        val entityType = EntityType.STUDENT
        val institutionId = "inst-456"

        // When
        receiverMetrics.recordEntityReceived(partnerId, entityType, institutionId)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("product")) == "receiver" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("entity_type")) == "Student" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("institution_id")) == institutionId
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica entity.processed com success=true")
    fun shouldRecordEntityProcessed() {
        // Given
        val partnerId = "partner-123"
        val entityType = EntityType.TEACHER

        // When
        receiverMetrics.recordEntityProcessed(partnerId, entityType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("entity_type")) == "Teacher" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == true
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica entity.failed com error_type")
    fun shouldRecordEntityFailed() {
        // Given
        val partnerId = "partner-123"
        val entityType = EntityType.INSTITUTION
        val errorType = "ValidationException"

        // When
        receiverMetrics.recordEntityFailed(partnerId, entityType, errorType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("entity_type")) == "Institution" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("error_type")) == errorType &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.booleanKey("success")) == false
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica entity.deleted")
    fun shouldRecordEntityDeleted() {
        // Given
        val partnerId = "partner-123"
        val entityType = EntityType.CLASSROOM

        // When
        receiverMetrics.recordEntityDeleted(partnerId, entityType)

        // Then
        verify {
            mockCounter.add(1, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("entity_type")) == "Classroom"
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métrica processing.duration em histograma")
    fun shouldRecordProcessingDuration() {
        // Given
        val partnerId = "partner-123"
        val entityType = EntityType.ADMIN
        val durationMs = 75.0
        val institutionId = "inst-789"

        // When
        receiverMetrics.recordProcessingDuration(partnerId, entityType, durationMs, institutionId)

        // Then
        verify {
            mockHistogram.record(durationMs, match<Attributes> { attrs ->
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("partner_id")) == partnerId &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("entity_type")) == "Admin" &&
                attrs.get(io.opentelemetry.api.common.AttributeKey.stringKey("institution_id")) == institutionId
            })
        }
    }

    @Test
    @DisplayName("Deve emitir métricas para todos os tipos de entidade")
    fun shouldRecordMetricsForAllEntityTypes() {
        // Given
        val partnerId = "partner-123"
        val entityTypes = listOf(
            EntityType.INSTITUTION,
            EntityType.CLASSROOM,
            EntityType.ADMIN,
            EntityType.COORDINATOR,
            EntityType.TEACHER,
            EntityType.STUDENT,
            EntityType.LICENSE
        )

        // When
        entityTypes.forEach { entityType ->
            receiverMetrics.recordEntityReceived(partnerId, entityType)
        }

        // Then
        verify(exactly = entityTypes.size) {
            mockCounter.add(1, any())
        }
    }
}
