package rs.edu.raf.cloud.backend.machine.model

import jakarta.persistence.*
import rs.edu.raf.cloud.backend.user.model.User
import java.time.LocalDateTime

@Entity
@Table(name = "machines")
class Machine(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: MachineStatus? = null,

    @Column(nullable = false)
    var active: Boolean = true, // For soft delete

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // Relationship to User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        // Constant hashcode for JPA entity consistency
        return 1337
    }

    override fun toString(): String {
        return "Machine(createdAt=$createdAt, active=$active, status=$status, name='$name', id=$id)"
    }

}

enum class MachineStatus {
    STOPPED, // "OFF" in your front, let's map it standardly or use OFF
    RUNNING, // "ON"
    STOPPING, // TURNING_OFF
    STARTING  // TURNING_ON
    // Note: Use the exact strings from your frontend or map them using @JsonValue
}

enum class MachineOperation(
    val validMachineStatus: MachineStatus,
) {
    START(MachineStatus.STOPPED), STOP(MachineStatus.RUNNING), RESTART(MachineStatus.RUNNING)
}