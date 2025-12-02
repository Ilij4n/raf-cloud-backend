package rs.edu.raf.cloud.backend.core.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenService: TokenService // This connects to the service we made earlier
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. Get the header
        val authHeader = request.getHeader("Authorization")

        // 2. Check if valid (Must start with "Bearer ")
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        // 3. Extract token
        val token = authHeader.substring(7)

        try {
            // 4. Validate & Extract data using your TokenService
            val claims = tokenService.extractClaims(token)

            // 5. Get User Details from the claims
            val email = claims.subject
            // Safe cast the permissions list
            val permissions = claims.get("permissions", List::class.java) as? List<String> ?: emptyList()

            // 6. Create Authentication Object for Spring Security
            // We map string permissions (e.g. "can_create_users") to Spring Authorities
            val authorities = permissions.map { SimpleGrantedAuthority(it) }

            val authToken = UsernamePasswordAuthenticationToken(
                email, // Principal (Who is this?)
                null,  // Credentials (we don't keep password in memory)
                authorities // Authorities (What can they do?)
            )

            authToken.details = WebAuthenticationDetailsSource().buildDetails(request)

            // 7. Save to SecurityContext
            // This is crucial! This makes "SecurityContextHolder.getContext().authentication" work in your Aspect later.
            SecurityContextHolder.getContext().authentication = authToken

        } catch (e: Exception) {
            // If token is invalid/expired, we simply don't set the authentication.
            // Spring Security will treat the user as "Anonymous" and block them if the route is protected.
            println("JWT Validation failed: ${e.message}")
        }

        // 8. Continue the filter chain
        filterChain.doFilter(request, response)
    }
}