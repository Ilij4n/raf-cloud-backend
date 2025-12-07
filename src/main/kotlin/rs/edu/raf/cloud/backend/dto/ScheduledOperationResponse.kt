package rs.edu.raf.cloud.backend.dto

import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import java.time.LocalDateTime

data class ScheduledOperationResponse(
    val id: Long,
    val date: LocalDateTime, // "scheduledAt" in DB
    val machineId: Long,
    val machineName: String, // Enriched data
    val operation: MachineOperation
)
