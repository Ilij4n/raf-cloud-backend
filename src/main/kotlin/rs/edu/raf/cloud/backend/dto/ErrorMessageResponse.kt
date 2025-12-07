package rs.edu.raf.cloud.backend.dto

import java.time.LocalDateTime

data class ErrorMessageResponse(
    val id: Long,
    val date: LocalDateTime,
    val machineId: Long,
    val machineName: String, // Enriched data
    val operation: String,
    val errorMessage: String
)
