package rs.edu.raf.cloud.backend.bootstrap

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository

@Component
class DataSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {

        // 1. Create ADMIN (If not exists)
        if (!userRepository.existsByEmail("admin@raf.rs")) {
            val admin = User(
                email = "admin@raf.rs",
                firstName = "Admin",
                lastName = "Admin",
                // Hash the password "admin"
                passwordHash = passwordEncoder.encode("admin"),
                // Give ALL permissions
                permissions = Permission.entries.toMutableSet()
            )
            userRepository.save(admin)
            println("DATA SEEDER: Admin user created (email: admin@raf.rs, pass: admin)")
        }

        // 2. Create REGULAR USER (If not exists)
        if (!userRepository.existsByEmail("user@raf.rs")) {
            val user = User(
                email = "user@raf.rs",
                firstName = "Marko",
                lastName = "Markovic",
                passwordHash = passwordEncoder.encode("user"),
                // Give limited permissions (e.g., Can read users and search/start machines, but not create/delete)
                permissions = mutableSetOf(
                    Permission.READ_USER,
                    Permission.SEARCH_MACHINE,
                    Permission.START_MACHINE,
                    Permission.STOP_MACHINE
                )
            )
            userRepository.save(user)
            println("DATA SEEDER: Regular user created (email: user@raf.rs, pass: user)")
        }

        // 3. Create NO-PERMS USER (To test the "403" or empty state screens)
        if (!userRepository.existsByEmail("noperms@raf.rs")) {
            val sadUser = User(
                email = "noperms@raf.rs",
                firstName = "Tuzni",
                lastName = "Korisnik",
                passwordHash = passwordEncoder.encode("user"),
                permissions = mutableSetOf() // Empty set
            )
            userRepository.save(sadUser)
            println("DATA SEEDER: No-permission user created (email: noperms@raf.rs, pass: user)")
        }
    }
}