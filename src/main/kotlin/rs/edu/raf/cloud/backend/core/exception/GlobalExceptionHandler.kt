package rs.edu.raf.cloud.backend.core.exception
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import rs.edu.raf.cloud.backend.core.util.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // 1. EXPECTED ERRORS (Validation)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage))
    }

    // 2. EXPECTED ERRORS (Our Business Logic)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        // We trust the message in BusinessException because WE wrote it.
        return ResponseEntity.status(ex.status).body(ApiResponse.error(ex.message))
    }

    // 3. EXPECTED ERRORS (Security Aspect)
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(ex.statusCode).body(ApiResponse.error(ex.reason ?: "Access Denied"))
    }

    // 4. UNEXPECTED ERRORS (NPEs, Database crashes, etc.)
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        // Log the full stack trace for the developer
        logger.error("Unexpected Internal Error: ", ex)

        // Send a SAFE, generic message to the user
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected internal error occurred. Please contact support."))
    }
}