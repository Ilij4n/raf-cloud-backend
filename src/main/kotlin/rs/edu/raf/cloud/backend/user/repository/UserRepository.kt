package rs.edu.raf.cloud.backend.user.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.user.model.User
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // JPA automatically generates the SQL for this
    fun findByEmail(email: String): Optional<User>

    // Check if email exists (for registration validation)
    fun existsByEmail(email: String): Boolean
}