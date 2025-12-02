package rs.edu.raf.cloud.backend.core.auth

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import rs.edu.raf.cloud.backend.user.model.User
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtTokenService(
    // todo Remove the @param if getting errors
    @param:Value("\${oauth.jwt.secret}") private val jwtSecret: String,
    @param:Value("\${oauth.jwt.expiration}") private val jwtExpirationMinutes: Long
) : TokenService {

    // Convert the string secret into a cryptographic Key object
    private fun getSigningKey(): SecretKey {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray(Charsets.UTF_8))
    }

    override fun generateToken(user: User): String {
        // Set the expiry date for the token. All requests made with an expired one will be considered invalid
        val now = Date()
        val expiryDate = Date(now.time + (jwtExpirationMinutes * 60 * 1000))

        // Map the perms onto the claims object
        val permissionsList = user.permissions.map { it.authority }

        return Jwts.builder()
            .subject(user.email) // The "ID"
            .claim("userId", user.id)
            .claim("permissions", permissionsList)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey()) // Sign it so it can't be tampered with
            .compact()
    }

    override fun extractClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .payload
    }

    override fun isValid(token: String): Boolean {
        try {
            // If parsing fails (expired, wrong signature), it throws an exception
            extractClaims(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}