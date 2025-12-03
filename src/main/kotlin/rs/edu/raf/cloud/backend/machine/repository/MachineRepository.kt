package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.machine.model.Machine
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import java.time.LocalDateTime
import java.util.Optional

@Repository
interface MachineRepository : JpaRepository<Machine, Long> {

    // The logic:
    // 1. Filter by soft delete (m.active = true)
    // 2. Filter by User ID (unless userId is null, which means Admin sees all)
    // 3. Filter by Name (if provided)
    // 4. Filter by Status (if provided)
    // 5. Filter by Date Range (if provided)
    @Query("""
        SELECT m FROM Machine m
        WHERE m.active = true
        AND (:userId IS NULL OR m.user.id = :userId)
        AND (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:statuses IS NULL OR m.status IN :statuses)
        AND (cast(:dateFrom as date) IS NULL OR m.createdAt >= :dateFrom)
        AND (cast(:dateTo as date) IS NULL OR m.createdAt <= :dateTo)
    """)
    fun search(
        @Param("userId") userId: Long?,
        @Param("name") name: String?,
        @Param("statuses") statuses: List<MachineStatus>?,
        @Param("dateFrom") dateFrom: LocalDateTime?,
        @Param("dateTo") dateTo: LocalDateTime?
    ): List<Machine>

    // Also useful helper
    fun findByIdAndActiveTrue(id: Long): Optional<Machine>
}