package rs.edu.raf.cloud.backend.core.exception
import org.springframework.http.HttpStatus

// We use this exception when WE want to show a message to the user.
class BusinessException(
    override val message: String,
    val status: HttpStatus = HttpStatus.BAD_REQUEST
) : RuntimeException(message)