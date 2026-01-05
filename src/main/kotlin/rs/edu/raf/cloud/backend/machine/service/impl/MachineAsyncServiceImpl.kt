package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.dto.MachineResponse
import rs.edu.raf.cloud.backend.machine.model.ErrorMessage
import rs.edu.raf.cloud.backend.machine.model.Machine
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.repository.ErrorMessageRepository
import rs.edu.raf.cloud.backend.machine.repository.MachineRepository
import java.time.LocalDateTime
import java.util.Random

@Service
class MachineAsyncService(
    private val machineRepository: MachineRepository,
    private val errorMessageRepository: ErrorMessageRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @Async("taskExecutor")
    fun startMachine(machineId: Long) {
        performAction(machineId, MachineOperation.START, MachineStatus.STARTING, MachineStatus.RUNNING)
    }

    @Async("taskExecutor")
    fun stopMachine(machineId: Long) {
        performAction(machineId, MachineOperation.STOP, MachineStatus.STOPPING, MachineStatus.STOPPED)
    }

    @Async("taskExecutor")
    fun restartMachine(machineId: Long) {
        try {

            updateAndNotify(machineId, MachineStatus.RESTARTING)

            val totalDelay = 10_000L + Random().nextInt(5000)
            val halfDelay = totalDelay / 2

            sleep(halfDelay)
            updateAndNotify(machineId, MachineStatus.STOPPED, expectedOldStatus = MachineStatus.RESTARTING)

            sleep(halfDelay)
            updateAndNotify(machineId, MachineStatus.RUNNING, expectedOldStatus = MachineStatus.STOPPED)

        } catch (e: ObjectOptimisticLockingFailureException) {
            println("Concurrency: Machine $machineId was updated by another thread during RESTART. Aborting.")
        } catch (e: Exception) {
            handleError(machineId, "RESTART", e.message ?: "Unknown error")
            // Attempt to revert to safe state (Running is usually the assumption if restart fails mid-way)
            try { updateAndNotify(machineId, MachineStatus.RUNNING) } catch (ignore: Exception) {}
        }
    }

    private fun performAction(
        machineId: Long,
        operation: MachineOperation,
        intermediateStatus: MachineStatus,
        finalStatus: MachineStatus
    ) {
        // Determine fallback state logic
        val revertStatus = if (operation == MachineOperation.START) MachineStatus.STOPPED else MachineStatus.RUNNING

        try {
            // Set Intermediate Status
            updateAndNotify(machineId, intermediateStatus)

            // Simulate Work
            sleep(10_000L + Random().nextInt(5000))

            // Note: We only update to final status IF the machine is still in intermediateStatus.
            // If another thread changed it while we slept, this throws ObjectOptimisticLockingFailureException.
            updateAndNotify(machineId, finalStatus, expectedOldStatus = intermediateStatus)

        } catch (e: ObjectOptimisticLockingFailureException) {
            println("Concurrency: Machine $machineId state changed externally during ${operation.name}. Aborting.")
        } catch (e: Exception) {
            handleError(machineId, operation.name, e.message ?: "Unknown error")
            // Revert
            try { updateAndNotify(machineId, revertStatus) } catch (ignore: Exception) {}
        }
    }

    // Updates the machine status, saves to DB, and pushes to WebSockets.
    // expectedOldStatus: If provided, ensures the DB state matches this before updating. This manually enforces consistency across async sleeps.
    fun updateAndNotify(machineId: Long, newStatus: MachineStatus, expectedOldStatus: MachineStatus? = null) {
        // Fetch Machine and user. Using join fetch query to avoid LazyInitException)
        val machine: Machine = machineRepository.findByIdWithUser(machineId)
            .orElseThrow { RuntimeException("Machine $machineId disappeared during execution") }

        // A rare race condition where two threads might enter the performaction method at the same time, bypassing the check in the machine service
        // If a thread updates the intermediary state while another thread is sleeping, this will trigger and the operation will be aborted, effectively allowing the last thread which entered
        // to execute its action
        if (expectedOldStatus != null && machine.status != expectedOldStatus) {
            // Throwing this triggers the catch block in performAction/restartMachine, aborting the task.
            throw ObjectOptimisticLockingFailureException(Machine::class.java, machine.id!!)
        }

        // Modify and save with version increment
        machine.status = newStatus
        // if another thread is updating the data at the same time, this will throw an ObjectOptimisticLockingFailureException due to optimistic lock
        machineRepository.save(machine)

        val dto = machine.toDto()
        // Notify Admin Channel
        messagingTemplate.convertAndSend("/topic/machines/admin", dto)

        // Notify Specific User Channel
        // Safe to access user because findByIdWithUser fetched it eagerly
        val userTopic = "/topic/machines/${machine.user!!.id}"
        messagingTemplate.convertAndSend(userTopic, dto)
    }

    private fun handleError(machineId: Long, operation: String, errorMsg: String) {
        val error = ErrorMessage(
            machineId = machineId,
            operation = operation,
            message = errorMsg,
            date = LocalDateTime.now()
        )
        errorMessageRepository.save(error)
    }

    private fun Machine.toDto(): MachineResponse {
        return MachineResponse(
            id = id!!,
            name = name,
            status = status!!,
            createdAt = createdAt,
            active = active,
            createdById = user?.id!!,
            createdBy = "${user?.firstName} ${user?.lastName}"
        )
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}