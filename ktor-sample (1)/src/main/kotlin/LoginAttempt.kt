package com.example

import java.time.LocalDateTime

data class LoginAttempt(
    val username: String,
    val timestamp: LocalDateTime,
    val successful: Boolean
)