package rs.edu.raf.cloud.backend.dto

import jakarta.validation.constraints.NotBlank

data class CreateMachineRequest(
    @field:NotBlank
    val name: String
)
