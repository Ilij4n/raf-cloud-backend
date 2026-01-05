package rs.edu.raf.cloud.backend.core.auth

import jakarta.servlet.http.HttpServletRequest
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import rs.edu.raf.cloud.backend.core.exception.BusinessException
import rs.edu.raf.cloud.backend.user.model.Permission

@Aspect
@Configuration
class SecurityAspect {

    @Around("@annotation(rs.edu.raf.cloud.backend.core.auth.CheckSecurity)")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {

        // Get the metadata of the method - annotation arguments (perms)
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        val checkSecurity = method.getAnnotation(CheckSecurity::class.java)
            ?: return joinPoint.proceed() // if there are no perms, let the user through

        // Fetch the claims from the context storage local to the http request from the HttpServletRequest object
        // These were set upon authentication in the AuthInterceptor class
        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val request: HttpServletRequest = requestAttributes.request

        val permissionsObj = request.getAttribute("permissions")
            ?: throw BusinessException("Unauthorized: No permissions found", HttpStatus.UNAUTHORIZED)

        // Extract perm strings
        val userPermissions = permissionsObj as List<String>
        val requiredPermissions = checkSecurity.permissions.map { it.authority }

        // User needs to have all the required perms to access the protected endpoint
        val hasPermission = requiredPermissions.all { it in userPermissions }

        if (!hasPermission) {
            throw BusinessException("You do not have permission to perform this action", HttpStatus.FORBIDDEN)
        }

        return joinPoint.proceed()
    }
}