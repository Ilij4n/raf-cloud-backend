package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.dto.ErrorMessageResponse
import rs.edu.raf.cloud.backend.machine.model.ErrorMessage

@Repository
interface ErrorMessageRepository : JpaRepository<ErrorMessage, Long> {

    // 1. For Admins: Join Error and Machine tables, return DTO directly
    @Query("""
        SELECT new rs.edu.raf.cloud.backend.dto.ErrorMessageResponse(
            e.id, e.date, e.machineId, m.name, e.operation, e.message
        )
        FROM ErrorMessage e, Machine m
        WHERE e.machineId = m.id
        ORDER BY e.date DESC
    """)
    fun findAllErrorsWithMachineNames(): List<ErrorMessageResponse>

    // 2. For Users: Same Join, but filtered by m.user.id
    @Query("""
        SELECT new rs.edu.raf.cloud.backend.dto.ErrorMessageResponse(
            e.id, e.date, e.machineId, m.name, e.operation, e.message
        )
        FROM ErrorMessage e, Machine m
        WHERE e.machineId = m.id
        AND m.user.id = :userId
        ORDER BY e.date DESC
    """)
    fun findErrorsForUser(@Param("userId") userId: Long): List<ErrorMessageResponse>
}