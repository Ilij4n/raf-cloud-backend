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

    // ... helper methods (toDto, sleepRandomly) ...

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
        try {
            updateAndNotify(machineId, intermediateStatus)
            sleepRandomly()
            updateAndNotify(machineId, finalStatus)
        } catch (e: Exception) {
            handleError(machineId, operation.name, e.message ?: "Unknown error")
        }
    }

    // REMOVED @Transactional to fix the warning.
    // machineRepository.save() is already transactional enough for this single operation.
    fun updateAndNotify(machineId: Long, status: MachineStatus) {
        val machine = machineRepository.findById(machineId).orElse(null) ?: return
        machine.status = status
        val savedMachine = machineRepository.save(machine)

        // Broadcast to WebSocket
        messagingTemplate.convertAndSend("/topic/machines", savedMachine.toDto())
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