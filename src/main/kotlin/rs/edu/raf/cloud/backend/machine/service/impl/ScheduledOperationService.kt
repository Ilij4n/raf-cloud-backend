package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.core.exception.BusinessException
import rs.edu.raf.cloud.backend.dto.ScheduledOperationResponse
import rs.edu.raf.cloud.backend.machine.repository.ScheduledOperationRepository
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository

@Service
class ScheduledOperationService(
    private val scheduledOperationRepository: ScheduledOperationRepository,
    private val userRepository: UserRepository
) {

    // --- READ ---
    fun getScheduledOperations(): List<ScheduledOperationResponse> {
        val user = getCurrentUser()

        return if (user.isAdmin()) {
            scheduledOperationRepository.findAllWithMachineNames()
        } else {
            scheduledOperationRepository.findAllByCreatorId(user.id!!)
        }
    }

    // --- DELETE (Cancel) ---
    fun cancelOperation(id: Long) {
        val user = getCurrentUser()
        val operation = scheduledOperationRepository.findById(id)
            .orElseThrow { BusinessException("Scheduled operation not found") }

        // Access Control:
        // Admin can cancel anything.
        // User can only cancel operations THEY created.
        if (!user.isAdmin() && operation.createdById != user.id) {
            throw BusinessException("You do not have permission to cancel this operation")
        }

        scheduledOperationRepository.delete(operation)
    }

    // --- Helpers (Same as before) ---
    private fun getCurrentUser(): User {
        val email = SecurityContextHolder.getContext().authentication.name
        return userRepository.findByEmail(email).orElseThrow()
    }

    private fun User.isAdmin(): Boolean {
        return this.permissions.size == Permission.entries.size
    }
}