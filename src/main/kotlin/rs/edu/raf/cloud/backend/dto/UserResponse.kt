package rs.edu.raf.cloud.backend.dto

import rs.edu.raf.cloud.backend.user.model.Permission

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val permissions: List<Permission>
)
