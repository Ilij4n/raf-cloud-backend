package rs.edu.raf.cloud.backend.bootstrap

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import rs.edu.raf.cloud.backend.core.auth.PasswordEncoder
import rs.edu.raf.cloud.backend.machine.model.MachineStatus
import rs.edu.raf.cloud.backend.machine.repository.MachineRepository
import rs.edu.raf.cloud.backend.user.model.Permission
import rs.edu.raf.cloud.backend.user.model.User
import rs.edu.raf.cloud.backend.user.repository.UserRepository

@Component
class DataSeeder(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val machineRepository: MachineRepository,
) : CommandLineRunner {

    // CommandLineRunner for inserting mock data
    override fun run(vararg args: String?) {

        // Admin user (all perms)
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

        // Regular user
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
                    Permission.CREATE_MACHINE,
                    Permission.STOP_MACHINE
                )
            )
            userRepository.save(user)
            println("DATA SEEDER: Regular user created (email: user@raf.rs, pass: user)")
        }

        // No perms user
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

        // Fix Zombie Machines
        val stuckMachines = machineRepository.findAllByStatusIn(
            listOf(MachineStatus.STARTING, MachineStatus.STOPPING, MachineStatus.RESTARTING)
        )

        if (stuckMachines.isNotEmpty()) {
            println("SANITY CHECK: Found ${stuckMachines.size} stuck machines. Resetting to STOPPED.")
            stuckMachines.forEach {
                it.status = MachineStatus.STOPPED // Or OFF, safe default
                machineRepository.save(it)
            }
        }
    }
}