package rs.edu.raf.cloud.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import rs.edu.raf.cloud.backend.core.auth.JwtAuthenticationFilter
import jakarta.servlet.http.HttpServletResponse

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    // 1. Password Encoder Bean (Used by UserService)
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    // 2. The Filter Chain (Rules)
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Disable CSRF (We use JWT, so we don't need it)
            .csrf { it.disable() }

            // Disable CORS (Allow frontend to connect) - Simplest setup for dev
            .cors { }

            // Stateless Session (Don't create JSESSIONID cookies)
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // Route Authorization
            .authorizeHttpRequests {
                // Public endpoints
                it.requestMatchers("/api/auth/login").permitAll()

                // Swagger UI (Optional, good to have open)
                it.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Everything else requires a valid token
                it.anyRequest().authenticated()
            }

            // Custom 401 Handling (Return JSON instead of HTML)
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = "application/json"
                    response.writer.write("""{"success":false, "message":"Unauthorized", "data":null}""")
                }
            }

            // Add our JWT Filter before the standard Spring authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}