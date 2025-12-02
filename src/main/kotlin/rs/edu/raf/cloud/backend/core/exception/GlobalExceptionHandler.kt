package rs.edu.raf.cloud.backend.core.exception
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import rs.edu.raf.cloud.backend.core.util.ApiResponse

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    // For handling validation errors (@Valid, @NotBlank, @Email)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        // Kotlin's 'joinToString' replaces the verbose Java Stream logic
        val errorMessage = ex.bindingResult.fieldErrors.joinToString(", ") { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        logger.error("Validation error: {}", errorMessage)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(ApiResponse.error(errorMessage))
    }

    // Handling generic runtime exceptions - usually will be manually thrown by us
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ApiResponse<Nothing>> {
        logger.error("Runtime exception occurred:", ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) // 400
            .body(ApiResponse.error(ex.message ?: "An unknown error occurred"))
    }

    // Possibly for exceptions to be thrown by the aspect
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN) // 401
            .body(ApiResponse.error("You do not have permission to perform this action."))
    }

    // Todo, the above is for perms, see what we will do for 401
}