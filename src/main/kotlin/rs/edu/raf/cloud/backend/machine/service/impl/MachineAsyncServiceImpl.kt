package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.messaging.simp.SimpMessagingTemplate
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

    // These functions are effectively there to simulate the delay and update the frontend states via socket
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

            Thread.sleep(halfDelay)
            updateAndNotify(machineId, MachineStatus.STOPPED)

            Thread.sleep(halfDelay)
            updateAndNotify(machineId, MachineStatus.RUNNING)

        } catch (e: Exception) {
            handleError(machineId, "RESTART", e.message ?: "Unknown error")
        }
    }

    private fun performAction(
        machineId: Long,
        operation: MachineOperation,
        intermediateStatus: MachineStatus,
        finalStatus: MachineStatus
    ) {
        // Get the status the machine had to be in beforehands in case of error
        val revertStatus = if (operation == MachineOperation.START) MachineStatus.STOPPED else MachineStatus.RUNNING

        try {
            updateAndNotify(machineId, intermediateStatus)
            sleepRandomly()
            updateAndNotify(machineId, finalStatus)
        } catch (e: Exception) {
            handleError(machineId, operation.name, e.message ?: "Unknown error")

            // Revert to original state
            updateAndNotify(machineId, revertStatus)
        }
    }

    fun updateAndNotify(machineId: Long, status: MachineStatus) {

        val machine = machineRepository.findByIdWithUser(machineId)
            .orElseThrow { RuntimeException("Machine $machineId disappeared during execution") }

        machine.status = status
        machineRepository.save(machine)

        messagingTemplate.convertAndSend("/topic/machines", machine.toDto())
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

    // ... Helper toDto and sleepRandomly ...
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

    private fun sleepRandomly() {
        val sleepTime = 10_000L + Random().nextInt(5000)
        try {
            Thread.sleep(sleepTime)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}