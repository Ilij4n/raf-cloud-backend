package rs.edu.raf.cloud.backend.machine.service.impl

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import rs.edu.raf.cloud.backend.machine.model.ErrorMessage
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
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

    // Runs every 30 seconds
    @Scheduled(fixedDelay = 1000 * 30)
    @Transactional
    fun processScheduledOperations() {
        val now = LocalDateTime.now()

        // Find all operations to be executed now
        val dueOperations = scheduledOperationRepository.findAllByScheduledAtLessThanEqual(now)

        if (dueOperations.isEmpty()) return

        println("SCHEDULER: Found ${dueOperations.size} operations to execute.")

        for (op in dueOperations) {
            try {
                val machine = machineRepository.findById(op.machine!!.id!!)
                    .orElse(null)

                if (machine == null) {
                    //Insert scheduled error to db
                    logError(
                        machineId = op.machine!!.id!!,
                        operation = op.operation.name,
                        message = "Machine no longer exists."
                    )
                    continue
                }

                // Validate machine state
                val requiredStatus = op.operation.validMachineStatus
                if (machine.status != requiredStatus) {
                    logError(
                        machineId = machine.id!!,
                        operation = op.operation.name,
                        message = "Scheduled execution failed. Machine status is ${machine.status}, but required $requiredStatus."
                    )
                    continue
                }

                // Trigger async logic - with this setup, the frontend listening to the topic is expected to receive machine state updates in real time
                when (op.operation) {
                    MachineOperation.START -> machineAsyncService.startMachine(machine.id!!)
                    MachineOperation.STOP -> machineAsyncService.stopMachine(machine.id!!)
                    MachineOperation.RESTART -> machineAsyncService.restartMachine(machine.id!!)
                }

                println("SCHEDULER: Executed ${op.operation} on Machine ${machine.id}")

            } catch (e: Exception) {
                logError(op.machine!!.id!!, op.operation.name, "Unexpected error: ${e.message}")
            } finally {
                // Remove the scheduled operation so it does not execute again
                scheduledOperationRepository.delete(op)
            }
        }
    }

    // A function which inserts the error caused by a failed scheduled operation into the database
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