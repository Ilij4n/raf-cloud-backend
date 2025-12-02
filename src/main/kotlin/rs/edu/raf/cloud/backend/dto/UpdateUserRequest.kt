package rs.edu.raf.cloud.backend.dto

import jakarta.validation.constraints.Email
import rs.edu.raf.cloud.backend.user.model.Permission

// Note that all the fields are nullable, we allow partial updates accounts
data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,

    @field:Email(message = "Invalid email format")
    val email: String,

    // Optional: If the admin leaves this blank, we don't change the password.
    // If they type something, we hash it and update it.
    val password: String? = null,

    val permissions: List<Permission>? = null
)
