package rs.edu.raf.cloud.backend.user.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rs.edu.raf.cloud.backend.core.auth.CheckSecurity
import rs.edu.raf.cloud.backend.core.util.ApiResponse
import rs.edu.raf.cloud.backend.dto.CreateUserRequest
import rs.edu.raf.cloud.backend.dto.LoginRequest
import rs.edu.raf.cloud.backend.dto.LoginResponse
import rs.edu.raf.cloud.backend.dto.UserResponse
import rs.edu.raf.cloud.backend.dto.UpdateUserRequest
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.service.UserService

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/auth/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val result = userService.login(request)
        return ResponseEntity.ok(
            ApiResponse.success(result, "Login successful")
        )
    }

    // CREATE
    @PostMapping("/users")
    @CheckSecurity(permissions = [Permission.CREATE_USER])
    fun createUser(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val result = userService.createUser(request)
        return ResponseEntity.ok(
            ApiResponse.success(result, "User created successfully")
        )
    }

    // READ ALL
    @GetMapping("/users")
    @CheckSecurity(permissions = [Permission.READ_USER])
    fun getAllUsers(): ResponseEntity<ApiResponse<List<UserResponse>>> {
        val result = userService.findAll()
        return ResponseEntity.ok(
            ApiResponse.success(result)
        )
    }

    // READ ONE (Needed for the 'Update' form on frontend)
    @GetMapping("/users/{id}")
    @CheckSecurity(permissions = [Permission.READ_USER, Permission.UPDATE_USER])
    fun getUser(@PathVariable id: Long): ResponseEntity<ApiResponse<UserResponse>> {
        val result = userService.findById(id)
        return ResponseEntity.ok(
            ApiResponse.success(result)
        )
    }

    // UPDATE
    @PutMapping("/users/{id}")
    @CheckSecurity(permissions = [Permission.UPDATE_USER])
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserResponse>> {
        val result = userService.updateUser(id, request)
        return ResponseEntity.ok(
            ApiResponse.success(result, "User updated successfully")
        )
    }

    // DELETE
    @DeleteMapping("/users/{id}")
    @CheckSecurity(permissions = [Permission.DELETE_USER])
    fun deleteUser(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        userService.deleteUser(id)
        return ResponseEntity.ok(
            ApiResponse.success(Unit, "User deleted successfully")
        )
    }
}
