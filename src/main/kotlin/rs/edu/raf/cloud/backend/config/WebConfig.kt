package rs.edu.raf.cloud.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authInterceptor: AuthInterceptor
) : WebMvcConfigurer {

    // Cors configuration - whitelist of client source urls enforced by the browser
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**") // for any endpoint on my back
            .allowedOrigins("http://localhost:4200") // allow this url
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // with these http actions
            .allowedHeaders("*") // with any header (in our case Bearer authorization header)
            .allowCredentials(true)
    }

    // Bind an interceptor to verify the payload of the http requests before it hits our spring controller
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor) // custom authorization interceptor
            // the interceptor is applied/not applied to these endpoints
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/auth/login")
            .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**")

        // Note: We don't need to exclude /ws/** explicitly here because
        // WebSockets use a different HandshakeHandler, but if you have issues, add it.
        // .excludePathPatterns("/ws/**")
    }
}