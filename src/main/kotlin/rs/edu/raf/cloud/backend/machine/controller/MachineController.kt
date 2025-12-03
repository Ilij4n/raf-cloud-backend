package rs.edu.raf.cloud.backend.machine.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import rs.edu.raf.cloud.backend.core.auth.CheckSecurity
import rs.edu.raf.cloud.backend.core.util.ApiResponse
import rs.edu.raf.cloud.backend.dto.MachineResponse
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
}