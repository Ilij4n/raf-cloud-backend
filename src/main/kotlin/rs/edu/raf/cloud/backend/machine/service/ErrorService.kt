package rs.edu.raf.cloud.backend.machine.service

import rs.edu.raf.cloud.backend.dto.ErrorMessageResponse

interface ErrorService {
    fun getErrorsForCurrentUser(): List<ErrorMessageResponse>
}