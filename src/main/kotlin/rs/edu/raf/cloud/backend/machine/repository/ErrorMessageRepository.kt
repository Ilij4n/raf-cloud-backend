package rs.edu.raf.cloud.backend.machine.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import rs.edu.raf.cloud.backend.machine.model.ErrorMessage

@Repository
interface ErrorMessageRepository : JpaRepository<ErrorMessage, Long>