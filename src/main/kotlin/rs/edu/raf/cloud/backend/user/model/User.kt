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
    CREATE_USER("CREATE_USER"),
    READ_USER("READ_USER"),
    UPDATE_USER("UPDATE_USER"),
    DELETE_USER("DELETE_USER"),

    // Machine permissions
    SEARCH_MACHINE("SEARCH_MACHINE"),
    START_MACHINE("START_MACHINE"),
    STOP_MACHINE("STOP_MACHINE"),
    RESTART_MACHINE("RESTART_MACHINE"),
    CREATE_MACHINE("CREATE_MACHINE"),
    DESTROY_MACHINE("DESTROY_MACHINE");

    override fun toString(): String {
        return authority
    }
}