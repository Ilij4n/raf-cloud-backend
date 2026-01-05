package rs.edu.raf.cloud.backend.core.auth

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

// Wrapper as to match the PasswordEncoder api from the spring.security used in previous iteration of the code
@Component
class PasswordEncoder {

    fun encode(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return BCrypt.checkpw(rawPassword, encodedPassword)
    }
}