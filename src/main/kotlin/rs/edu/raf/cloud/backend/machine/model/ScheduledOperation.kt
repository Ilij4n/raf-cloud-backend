package rs.edu.raf.cloud.backend.machine.model

import jakarta.persistence.*
import rs.edu.raf.cloud.backend.user.model.User
import java.time.LocalDateTime
@Entity
@Table(name = "scheduled_operations")
class ScheduledOperation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var scheduledAt: LocalDateTime = LocalDateTime.now(), // Default value added

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var operation: MachineOperation = MachineOperation.START, // Default value added

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    var machine: Machine? = null, // Default value added (and nullable for safety)

    @Column(nullable = false)
    var createdById: Long = 0 // Default value added
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode() = 1337

    override fun toString() = "(id=$id, operation=$operation, scheduledAt=$scheduledAt, createdById=$createdById)"
}