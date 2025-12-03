package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.dto.MachineResponse
import rs.edu.raf.cloud.backend.machine.model.Machine
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.repository.MachineRepository
import rs.edu.raf.cloud.backend.machine.service.MachineService
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository
import java.time.LocalDateTime

@Service
class MachineServiceImpl(
    private val machineRepository: MachineRepository,
    private val userRepository: UserRepository
) : MachineService {

    override fun search(
        name: String?,
        statuses: List<MachineStatus>?,
        dateFrom: LocalDateTime?,
        dateTo: LocalDateTime?
    ): List<MachineResponse> {

        // Fetch the user id from the contextholder
        val auth = SecurityContextHolder.getContext().authentication
        val email = auth.name
        val user = userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found") }

        // Search with null parameter returns all machines, so we use this for admin
        val userIdFilter = if (user.isAdmin()) null else user.id

        val machines = machineRepository.search(
            userId = userIdFilter,
            name = name,
            statuses = statuses,
            dateFrom = dateFrom,
            dateTo = dateTo
        )

        return machines.map { it.toDto() }
    }

    // If the user has all of the permissions, he is considered an admin
    private fun User.isAdmin() = permissions.size == Permission.entries.size


    private fun Machine.toDto(): MachineResponse {
        return MachineResponse(
            id = this.id!!,
            name = this.name,
            status = this.status,
            createdAt = this.createdAt,
            active = this.active,
            createdById = this.user.id!!,
            createdBy = "${this.user.firstName} ${this.user.lastName}" // Or email
        )
    }
}