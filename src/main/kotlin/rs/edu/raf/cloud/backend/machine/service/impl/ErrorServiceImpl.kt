package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.core.exception.BusinessException
import rs.edu.raf.cloud.backend.dto.ErrorMessageResponse
import rs.edu.raf.cloud.backend.machine.repository.ErrorMessageRepository
import rs.edu.raf.cloud.backend.machine.service.ErrorService
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository

@Service
class ErrorServiceImpl(
    private val errorMessageRepository: ErrorMessageRepository,
    private val userRepository: UserRepository
) : ErrorService {

    override fun getErrorsForCurrentUser(): List<ErrorMessageResponse> {
        val user = getCurrentUser()

        return if (user.isAdmin()) {
            errorMessageRepository.findAllErrorsWithMachineNames()
        } else {
            errorMessageRepository.findErrorsForUser(user.id!!)
        }
    }

    // --- Helpers ---

    private fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByEmail(email)
            .orElseThrow { BusinessException("User not found") }
    }

    private fun User.isAdmin(): Boolean {
        return this.permissions.size == Permission.entries.size
    }
}