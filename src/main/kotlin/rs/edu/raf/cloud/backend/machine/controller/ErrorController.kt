package rs.edu.raf.cloud.backend.machine.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import rs.edu.raf.cloud.backend.core.util.ApiResponse
import rs.edu.raf.cloud.backend.dto.ErrorMessageResponse
import rs.edu.raf.cloud.backend.machine.service.ErrorService

@RestController
@RequestMapping("/api/errors")
class ErrorController(
    private val errorService: ErrorService
) {

    @GetMapping
    fun getErrors(): ResponseEntity<ApiResponse<List<ErrorMessageResponse>>> {
        val result = errorService.getErrorsForCurrentUser()
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}