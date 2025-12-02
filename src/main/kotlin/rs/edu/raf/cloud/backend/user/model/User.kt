package rs.edu.raf.cloud.backend.user.model
import jakarta.persistence.*
@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var firstName: String = "",

    @Column(nullable = false)
    var lastName: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    var permissions: MutableSet<Permission> = mutableSetOf() // Already has default

){
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
        return "User(id=$id, email='$email')"
    }
}

enum class Permission(val authority: String) {
    // User permissions
    CREATE_USER("can_create_users"),
    READ_USER("can_read_users"),
    UPDATE_USER("can_update_users"),
    DELETE_USER("can_delete_users"),

    // Machine permissions
    SEARCH_MACHINE("can_search_machines"),
    START_MACHINE("can_start_machines"),
    STOP_MACHINE("can_stop_machines"),
    RESTART_MACHINE("can_restart_machines"),
    CREATE_MACHINE("can_create_machines"),
    DESTROY_MACHINE("can_destroy_machines"); // "Destroy" vs "Delete" as per spec

    // Helper to allow Spring Security to see these as "Authorities"
    override fun toString(): String {
        return authority
    }
}