package rs.edu.raf.cloud.backend.machine.controller

import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import rs.edu.raf.cloud.backend.core.auth.CheckSecurity
import rs.edu.raf.cloud.backend.core.util.ApiResponse
import rs.edu.raf.cloud.backend.dto.CreateMachineRequest
import rs.edu.raf.cloud.backend.dto.MachineActionRequest
import rs.edu.raf.cloud.backend.dto.MachineResponse
import rs.edu.raf.cloud.backend.machine.model.MachineOperation
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.service.impl.MachineServiceImpl
import rs.edu.raf.cloud.backend.user.model.Permission
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/machines")
class MachineController(
    private val machineService: MachineServiceImpl
) {

    @CheckSecurity(permissions = [Permission.SEARCH_MACHINE])
    @GetMapping
    fun searchMachines(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) status: List<MachineStatus>?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) dateFrom: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) dateTo: LocalDateTime?
    ): ResponseEntity<ApiResponse<List<MachineResponse>>> {

        val result = machineService.search(name, status, dateFrom, dateTo)
        return ResponseEntity.ok(ApiResponse.success(result))
    }

    // --- CREATE ---
    @CheckSecurity(permissions = [Permission.CREATE_MACHINE])
    @PostMapping
    fun createMachine(@Valid @RequestBody request: CreateMachineRequest): ResponseEntity<ApiResponse<MachineResponse>> {
        val result = machineService.createMachine(request)
        return ResponseEntity.ok(ApiResponse.success(result, "Machine created"))
    }


    @CheckSecurity(permissions = [Permission.DESTROY_MACHINE])
    @DeleteMapping("/{id}")
    fun destroyMachine(@PathVariable id: Long): ResponseEntity<ApiResponse<Unit>> {
        machineService.destroyMachine(id)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Machine destroyed"))
    }

    @CheckSecurity(permissions = [Permission.START_MACHINE])
    @PostMapping("/{id}/start")
    fun startMachine(@PathVariable id: Long, @RequestBody request: MachineActionRequest): ResponseEntity<ApiResponse<Unit>> {
        // Delegates to the unified method
        machineService.performAction(id, MachineOperation.START, request.date)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Start action initiated"))
    }

    @CheckSecurity(permissions = [Permission.STOP_MACHINE])
    @PostMapping("/{id}/stop")
    fun stopMachine(@PathVariable id: Long, @RequestBody request: MachineActionRequest): ResponseEntity<ApiResponse<Unit>> {
        machineService.performAction(id, MachineOperation.STOP, request.date)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Stop action initiated"))
    }

    @CheckSecurity(permissions = [Permission.RESTART_MACHINE])
    @PostMapping("/{id}/restart")
    fun restartMachine(@PathVariable id: Long, @RequestBody request: MachineActionRequest): ResponseEntity<ApiResponse<Unit>> {
        machineService.performAction(id, MachineOperation.RESTART, request.date)
        return ResponseEntity.ok(ApiResponse.success(Unit, "Restart action initiated"))
    }
}