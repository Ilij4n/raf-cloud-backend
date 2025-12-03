package rs.edu.raf.cloud.backend.dto

import java.time.LocalDateTime

data class MachineActionRequest(
    // Optional. If null = Instant. If future = Scheduled.
    val date: LocalDateTime? = null
)
