package rs.edu.raf.cloud.backend.core.auth

import io.jsonwebtoken.Claims
import rs.edu.raf.cloud.backend.user.model.User

interface TokenService {
    // Used by UserService upon login
    fun generateToken(user: User): String

    // Used by SecurityFilter to check requests
    fun extractClaims(token: String): Claims

    // Check if valid
    fun isValid(token: String): Boolean
}