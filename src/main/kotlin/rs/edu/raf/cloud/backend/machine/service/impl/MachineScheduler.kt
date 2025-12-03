package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rs.edu.raf.cloud.backend.machine.model.ErrorMessage
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.repository.ErrorMessageRepository
import rs.edu.raf.cloud.backend.machine.repository.MachineRepository
import rs.edu.raf.cloud.backend.machine.repository.ScheduledOperationRepository
import java.time.LocalDateTime

@Component
class MachineScheduler(
    private val scheduledOperationRepository: ScheduledOperationRepository,
    private val machineRepository: MachineRepository,
    private val machineAsyncService: MachineAsyncService,
    private val errorMessageRepository: ErrorMessageRepository
) {

    // Runs every 2 seconds (2000ms)
    @Scheduled(fixedDelay = 2000)
    @Transactional
    fun processScheduledOperations() {
        val now = LocalDateTime.now()

        // 1. Find operations that are due (scheduledAt <= Now)
        // Note: You need to add this method to your ScheduledOperationRepository (see below)
        val dueOperations = scheduledOperationRepository.findAllByScheduledAtLessThanEqual(now)

        if (dueOperations.isEmpty()) return

        println("SCHEDULER: Found ${dueOperations.size} operations to execute.")

        for (op in dueOperations) {
            try {
                // 2. Fetch the machine (Fresh state)
                val machine = machineRepository.findById(op.machine!!.id!!)
                    .orElse(null)

                if (machine == null) {
                    logError(op.machine!!.id!!, op.operation.name, "Machine no longer exists.")
                    continue
                }

                // 3. Validate State (Is it still valid to perform this?)
                val requiredStatus = op.operation.validMachineStatus
                if (machine.status != requiredStatus) {
                    logError(
                        machine.id!!,
                        op.operation.name,
                        "Scheduled execution failed. Machine status is ${machine.status}, but required $requiredStatus."
                    )
                    continue
                }

                // 4. Execute (Trigger Async)
                when (op.operation) {
                    MachineOperation.START -> machineAsyncService.startMachine(machine.id!!)
                    MachineOperation.STOP -> machineAsyncService.stopMachine(machine.id!!)
                    MachineOperation.RESTART -> machineAsyncService.restartMachine(machine.id!!)
                }

                println("SCHEDULER: Executed ${op.operation} on Machine ${machine.id}")

            } catch (e: Exception) {
                logError(op.machine!!.id!!, op.operation.name, "Unexpected error: ${e.message}")
            } finally {
                // 5. Cleanup - Remove the schedule so it doesn't run again
                scheduledOperationRepository.delete(op)
            }
        }
    }

    private fun logError(machineId: Long, operation: String, message: String) {
        val error = ErrorMessage(
            machineId = machineId,
            operation = operation,
            message = message,
            date = LocalDateTime.now()
        )
        errorMessageRepository.save(error)
        println("SCHEDULER ERROR: $message")
    }
}