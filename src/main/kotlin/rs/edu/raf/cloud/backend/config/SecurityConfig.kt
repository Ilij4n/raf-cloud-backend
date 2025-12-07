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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

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
                it.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // ALLOW WEBSOCKET HANDSHAKE
                it.requestMatchers("/ws/**").permitAll() // <--- ADD THIS

                // Everything else requires auth
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

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 1. Allow your Frontend Origin
        configuration.allowedOrigins = listOf("http://localhost:4200")

        // 2. Allow HTTP Methods
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // 3. Allow Headers (specifically Authorization for the JWT)
        configuration.allowedHeaders = listOf("Authorization", "Content-Type")

        // 4. Allow Credentials (if you ever needed cookies, irrelevant for JWT but good practice)
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}