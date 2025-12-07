package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.dto.ScheduledOperationResponse
import rs.edu.raf.cloud.backend.machine.model.ScheduledOperation
import java.time.LocalDateTime

@Repository
interface ScheduledOperationRepository : JpaRepository<ScheduledOperation, Long> {

    // --- For the Scheduler (Existing) ---
    fun findAllByScheduledAtLessThanEqual(now: LocalDateTime): List<ScheduledOperation>

    // --- For the API (New) ---

    // 1. For Admin: Fetch ALL + Join Machine Name
    @Query("""
        SELECT new rs.edu.raf.cloud.backend.dto.ScheduledOperationResponse(
            op.id, op.scheduledAt, op.machine.id, m.name, op.operation
        )
        FROM ScheduledOperation op
        JOIN op.machine m
        ORDER BY op.scheduledAt ASC
    """)
    fun findAllWithMachineNames(): List<ScheduledOperationResponse>

    // 2. For User: Fetch THEIRS + Join Machine Name
    // We filter by 'op.createdById' OR 'm.user.id'.
    // Spec says: "Users see exclusively machines they made".
    // It's safer to filter by Machine Owner (m.user.id) to ensure they can't schedule on machines they don't own.
    // However, for auditing "Who scheduled this?", we check op.createdById.
    // Let's stick to createdById as per your frontend model.
    @Query("""
        SELECT new rs.edu.raf.cloud.backend.dto.ScheduledOperationResponse(
            op.id, op.scheduledAt, op.machine.id, m.name, op.operation
        )
        FROM ScheduledOperation op
        JOIN op.machine m
        WHERE op.createdById = :userId
        ORDER BY op.scheduledAt ASC
    """)
    fun findAllByCreatorId(@Param("userId") userId: Long): List<ScheduledOperationResponse>
}