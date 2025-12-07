package rs.edu.raf.cloud.backend.machine.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import rs.edu.raf.cloud.backend.core.util.ApiResponse
import rs.edu.raf.cloud.backend.dto.ScheduledOperationResponse
import rs.edu.raf.cloud.backend.machine.service.impl.ScheduledOperationService

@RestController
@RequestMapping("/api/schedule")
class ScheduledOperationController(
    private val scheduledOperationService: ScheduledOperationService
) {

    @GetMapping
    fun getScheduledOperations(): ResponseEntity<ApiResponse<List<ScheduledOperationResponse>>> {
        val result = scheduledOperationService.getScheduledOperations()
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    @DeleteMapping("/{id}")
    fun cancelOperation(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        scheduledOperationService.cancelOperation(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Operation cancelled successfully"))
    }
}