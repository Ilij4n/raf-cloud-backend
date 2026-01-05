package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
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
        // 1. Get Request from Thread
        val requestAttributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw BusinessException("Request context missing", HttpStatus.INTERNAL_SERVER_ERROR)

        val request = requestAttributes.request

        // 2. Get Email from Attribute (Set by AuthInterceptor)
        val email = request.getAttribute("userEmail") as? String
            ?: throw BusinessException("User context missing or user not authenticated", HttpStatus.UNAUTHORIZED)

        // 3. Fetch User
        return userRepository.findByEmail(email)
            .orElseThrow { BusinessException("User not found") }
    }

    private fun User.isAdmin(): Boolean {
        return this.permissions.size == Permission.entries.size
    }
}