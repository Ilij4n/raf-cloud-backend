package rs.edu.raf.cloud.backend.core.auth

import rs.edu.raf.cloud.backend.user.model.Permission

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CheckSecurity(
    val permissions: Array<Permission>
)