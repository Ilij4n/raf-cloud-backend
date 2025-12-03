package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.machine.model.ScheduledOperation

@Repository
interface ScheduledOperationRepository : JpaRepository<ScheduledOperation, Long> {
    // We will need this later for the Scheduler to find tasks due for execution
    // e.g., findAllByScheduledAtLessThanEqual(LocalDateTime now)
}