package rs.edu.raf.cloud.backend.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import rs.edu.raf.cloud.backend.core.auth.TokenService
import rs.edu.raf.cloud.backend.core.exception.BusinessException
import org.springframework.http.HttpStatus

@Component
class AuthInterceptor(
    private val tokenService: TokenService
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // Browsers send an OPTIONS request before POST/PUT/DELETE. It has no token. We must let it pass.
        if (request.method == "OPTIONS") return true

        // Extract token
        val authHeader = request.getHeader("Authorization")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Check the structure of the bearer token, if its malformed, send 401 response
            throw BusinessException("Unauthorized: Missing token", HttpStatus.UNAUTHORIZED)
        }

        val token = authHeader.substring(7)

        try {
            // Verify the token signature and expiry, then parse the claims
            val claims = tokenService.extractClaims(token)

            // Store in HttpServletRequest, essentially a component which contains context for a given http request
            request.setAttribute("userEmail", claims.subject)
            request.setAttribute("userId", claims["userId"])
            request.setAttribute("permissions", claims["permissions"])

            return true // Proceed to Controller

        } catch (e: Exception) {
            // Token expired or invalid signature
            throw BusinessException("Unauthorized: Invalid token", HttpStatus.UNAUTHORIZED)
        }
    }
}