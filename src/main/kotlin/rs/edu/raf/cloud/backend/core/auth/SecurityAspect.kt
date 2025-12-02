package rs.edu.raf.cloud.backend.core.auth

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.server.ResponseStatusException

import rs.edu.raf.cloud.backend.user.model.Permission
@Aspect
@Configuration
class SecurityAspect {

    @Around("@annotation(rs.edu.raf.cloud.backend.core.auth.CheckSecurity)")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        // 1. Get the method signature using Reflection
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        // 2. Get the annotation
        val checkSecurity = method.getAnnotation(CheckSecurity::class.java)
            ?: return joinPoint.proceed()

        // 3. Get the Current User (from Spring Security Context)
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated")
        }

        // 4. Get User's Permissions (We stored them as Authorities in the JwtFilter)
        val userAuthorities = authentication.authorities.map { it.authority }

        // 5. Check if User has AT LEAST ONE of the required permissions
        // (Or ALL, depending on your logic. Usually "Any" is standard).
        val requiredPermissions = checkSecurity.permissions.map { it.authority }

        val hasPermission = requiredPermissions.any { it in userAuthorities }

        if (!hasPermission) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.")
        }

        // 6. Proceed
        return joinPoint.proceed()
    }
}