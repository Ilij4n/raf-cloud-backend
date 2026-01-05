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

    // EXPECTED ERRORS (Validation)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val errorMessage = ex.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage)) // 400 bad request
    }

    // EXPECTED ERRORS (Business Logic)
    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(ex: BusinessException): ResponseEntity<ApiResponse<Nothing>> {
        // We trust the message in BusinessException because WE wrote it.
        return ResponseEntity.status(ex.status).body(ApiResponse.error(ex.message))
    }

    // EXPECTED ERRORS (Security Aspect)
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(ex: ResponseStatusException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(ex.statusCode).body(ApiResponse.error(ex.reason ?: "Access Denied")) // 403 forbidden
    }

    // UNEXPECTED ERRORS (NPEs, Database crashes...)
    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {

        logger.error("Unexpected Internal Error: ", ex)

        // Generic message
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal server error
            .body(ApiResponse.error("An unexpected internal error occurred. Please contact support."))
    }
}