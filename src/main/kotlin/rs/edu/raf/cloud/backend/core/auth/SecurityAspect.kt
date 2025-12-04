package rs.edu.raf.cloud.backend.core.auth

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

@Aspect
@Configuration
class SecurityAspect {

    @Around("@annotation(rs.edu.raf.cloud.backend.core.auth.CheckSecurity)")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        // Get the method signature using Reflection
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        // Get the annotation
        val checkSecurity = method.getAnnotation(CheckSecurity::class.java)
            ?: return joinPoint.proceed()

        // Get the Current User from spring security context
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated")
        }

        // Get User's Permissions (We stored them as Authorities in the JwtFilter)
        val userAuthorities = authentication.authorities.map { it.authority }

        // Check if the user has the required permissions
        val requiredPermissions = checkSecurity.permissions.map { it.authority }
        val hasPermission = requiredPermissions.all { it in userAuthorities }

        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.")
        }

        return joinPoint.proceed()
    }
}