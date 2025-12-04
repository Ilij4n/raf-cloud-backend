package rs.edu.raf.cloud.backend.machine.service

import rs.edu.raf.cloud.backend.dto.MachineResponse
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import java.time.LocalDateTime

interface MachineService {
    fun search(
        name: String?,
        statuses: List<MachineStatus>?,
        dateFrom: LocalDateTime?,
        dateTo: LocalDateTime?
    ): List<MachineResponse>

    fun scheduleAction(
        id: Long,
        operation: MachineOperation,
        scheduledAt: LocalDateTime?
    )
}