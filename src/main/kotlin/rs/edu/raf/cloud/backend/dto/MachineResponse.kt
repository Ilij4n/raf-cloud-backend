package rs.edu.raf.cloud.backend.dto

import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import java.time.LocalDateTime

data class MachineResponse(
    val id: Long,
    val name: String,
    val status: MachineStatus,
    val createdAt: LocalDateTime,
    val active: Boolean,

    // User details (flattened for the frontend)
    val createdById: Long,
    val createdBy: String // e.g. "Marko Markovic" or email
)
