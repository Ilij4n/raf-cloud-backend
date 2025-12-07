package rs.edu.raf.cloud.backend.user.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.core.auth.TokenService
import rs.edu.raf.cloud.backend.core.exception.BusinessException
import rs.edu.raf.cloud.backend.dto.*
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository
import rs.edu.raf.cloud.backend.user.service.UserService

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val tokenService: TokenService,
    private val passwordEncoder: PasswordEncoder // We will configure this bean in the next step
) : UserService {

    override fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { BusinessException("User not found") }

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw BusinessException("Invalid password")
        }

        // Generate JWT
        val token = tokenService.generateToken(user)
        return LoginResponse(token)
    }

    override fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw BusinessException("Email already in use")
        }

        val newUser = User(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            // Hash the password before saving!
            passwordHash = passwordEncoder.encode(request.password),
            permissions = request.permissions.toMutableSet()
        )

        val savedUser = userRepository.save(newUser)
        return savedUser.toDto()
    }

    override fun findAll(): List<UserResponse> {
        return userRepository.findAll().map { it.toDto() }
    }

    override fun findById(id: Long): UserResponse {
        return userRepository.findById(id)
            .orElseThrow { BusinessException("User not found with id: $id") }
            .toDto()
    }

    override fun updateUser(id: Long, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { BusinessException("User not found with id: $id") }

        // Update fields only
        // Since request fields are val (immutable) but user fields are var (mutable), just assign
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.email = request.email // Note: Real apps often restrict changing email, but specs didn't forbid it

        // If a new password was provided, hash it. If null, keep old hash.
        if (request.password != null) {
            user.passwordHash = passwordEncoder.encode(request.password)
        }

        // If permissions provided, update them.
        if (request.permissions != null) {
            user.permissions.clear()
            user.permissions.addAll(request.permissions)
        }

        val updatedUser = userRepository.save(user)
        return updatedUser.toDto()
    }

    override fun deleteUser(id: Long) {
        val user = userRepository.findById(id)
        if (user.isEmpty)
            throw BusinessException("User not found with id: $id")

        val userEmail = SecurityContextHolder.getContext().authentication.name
        if(user.get().email == userEmail)
            throw BusinessException("Cannot delete your own account")

        userRepository.deleteById(id)
    }

    // Usually this would be in a separate file
    private fun User.toDto(): UserResponse {
        return UserResponse(
            id = this.id!!, // We know ID is not null if it came from DB
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            permissions = this.permissions.toList()
        )
    }
}