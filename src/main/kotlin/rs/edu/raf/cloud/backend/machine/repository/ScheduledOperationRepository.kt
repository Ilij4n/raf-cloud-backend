package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.machine.model.ScheduledOperation
import java.time.LocalDateTime

@Repository
interface ScheduledOperationRepository : JpaRepository<ScheduledOperation, Long> {
    fun findAllByScheduledAtLessThanEqual(now: LocalDateTime): List<ScheduledOperation>
}