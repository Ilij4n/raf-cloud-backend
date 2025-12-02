package rs.edu.raf.cloud.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RafCloudBackendApplication

fun main(args: Array<String>) {
	runApplication<RafCloudBackendApplication>(*args)
}
