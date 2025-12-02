package rs.edu.raf.cloud.backend.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import rs.edu.raf.cloud.backend.user.model.Permission

data class CreateUserRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "First name is required")
    val firstName: String,

    @field:NotBlank(message = "Last name is required")
    val lastName: String,

    @field:NotBlank(message = "Password is required")
    val password: String,

    // We use the Enum here. Jackson will automatically map strings
    // like "CREATE_USER" from JSON to this Enum.
    val permissions: List<Permission> = emptyList()
)
