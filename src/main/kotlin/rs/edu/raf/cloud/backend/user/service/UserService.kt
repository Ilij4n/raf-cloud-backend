package rs.edu.raf.cloud.backend.user.service

import rs.edu.raf.cloud.backend.dto.CreateUserRequest
import rs.edu.raf.cloud.backend.dto.LoginRequest
import rs.edu.raf.cloud.backend.dto.LoginResponse
import rs.edu.raf.cloud.backend.dto.UserResponse
import rs.edu.raf.cloud.backend.dto.UpdateUserRequest

interface UserService {
    fun login(request: LoginRequest): LoginResponse

    // Via create user button on the list component
    fun createUser(request: CreateUserRequest): UserResponse

    // Most likely will be called by the edit modal upon hitting edit on a given user
    fun findById(id: Long): UserResponse

    // For fetching the whole list contents
    fun findAll(): List<UserResponse>

    // For edit functionality (opened via modal)
    fun updateUser(id: Long, request: UpdateUserRequest): UserResponse

    fun deleteUser(id: Long)
}