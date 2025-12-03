package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.dto.CreateMachineRequest
import rs.edu.raf.cloud.backend.dto.MachineResponse
import rs.edu.raf.cloud.backend.machine.model.Machine
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.model.ScheduledOperation
import rs.edu.raf.cloud.backend.machine.repository.MachineRepository
import rs.edu.raf.cloud.backend.machine.repository.ScheduledOperationRepository
import rs.edu.raf.cloud.backend.machine.service.MachineService
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository
import java.time.LocalDateTime

@Service
class MachineServiceImpl( // Changed from MachineServiceImpl to MachineService (if you aren't using an interface explicitly, class is fine)
    private val machineRepository: MachineRepository,
    private val userRepository: UserRepository,
    private val scheduledOperationRepository: ScheduledOperationRepository // <--- ADDED THIS
) : MachineService {

    override fun search(
        name: String?,
        statuses: List<MachineStatus>?,
        dateFrom: LocalDateTime?,
        dateTo: LocalDateTime?
    ): List<MachineResponse> {
        val user = findUserOrThrow()

        // Admin sees all (null filter), User sees only theirs (id filter)
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

    override fun performAction(id: Long, operation: MachineOperation, scheduledAt: LocalDateTime?) {
        // Optimization: Get user once here if needed, but your helpers are fine for now.
        val machine = findMachineWithAccessControl(id)

        // 1. Check if it is a Scheduled Future Task
        if (scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now())) {
            val scheduledOp = ScheduledOperation(
                scheduledAt = scheduledAt,
                operation = operation,
                machine = machine,
                createdById = getCurrentUserId()
            )
            scheduledOperationRepository.save(scheduledOp)
            return
        }

        if (machine.status != operation.validMachineStatus)
            throw RuntimeException("Machine must be in state ${operation.validMachineStatus} to perform ${operation.name}.")

        // 3. Trigger Async Execution
        // We will implement this next!
        // asyncService.executeAsyncAction(machine.id!!, operation)
        println("DEBUG: Executing instant action $operation on machine $id")
    }


    fun createMachine(request: CreateMachineRequest): MachineResponse {
        val user = findUserOrThrow()
        // Note: Spec implies user creates machine for themselves.
        // If Admin creates it, does it belong to Admin? Usually yes.

        val machine = Machine(
            name = request.name,
            status = MachineStatus.STOPPED,
            active = true,
            user = user
        )
        return machineRepository.save(machine).toDto()
    }

    fun destroyMachine(id: Long) {
        val machine = findMachineWithAccessControl(id)
        if (machine.status != MachineStatus.STOPPED) {
            throw RuntimeException("Machine must be STOPPED to be destroyed")
        }
        machine.active = false // Soft delete
        machineRepository.save(machine)
    }

    // --- HELPER FUNCTIONS ---

    private fun User.isAdmin() = permissions.size == Permission.entries.size

    private fun findUserOrThrow(): User {
        val auth = SecurityContextHolder.getContext().authentication
        val email = auth.name
        return userRepository.findByEmail(email)
            .orElseThrow { RuntimeException("User not found") }
    }

    private fun getCurrentUserId(): Long = findUserOrThrow().id!!

    private fun findMachineWithAccessControl(id: Long): Machine {
        val user = findUserOrThrow()

        val machine = machineRepository.findByIdAndActiveTrue(id)
            .orElseThrow { RuntimeException("Machine not found") }

        // Access Control: If not admin and not owner -> Block
        if (!user.isAdmin() && machine.user?.id != user.id) {
            throw RuntimeException("You do not have access to this machine")
        }
        return machine
    }

    private fun Machine.toDto(): MachineResponse {
        return MachineResponse(
            id = this.id!!,
            name = this.name,
            status = this.status!!,
            createdAt = this.createdAt,
            active = this.active,
            createdById = this.user?.id!!,
            createdBy = "${this.user?.firstName} ${this.user?.lastName}"
        )
    }
}

