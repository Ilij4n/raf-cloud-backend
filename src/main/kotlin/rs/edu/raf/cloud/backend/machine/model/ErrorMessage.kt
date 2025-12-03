package rs.edu.raf.cloud.backend.machine.model

import jakarta.persistence.*
import rs.edu.raf.cloud.backend.user.model.User
import java.time.LocalDateTime

@Entity
@Table(name = "error_messages")
class ErrorMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var date: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var machineId: Long? = null,

    @Column(nullable = false)
    var operation: String = "",

    @Column(nullable = false)
    var message: String = ""
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode() = 1337

    override fun toString() = ""
}