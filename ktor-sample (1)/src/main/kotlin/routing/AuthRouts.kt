package com.example.routing


import com.example.AuthResponse
import com.example.Services.BruteForceProtectionService
import com.example.Services.JwtService
import com.example.AuthRequest
import com.example.repositories.UserRepository
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

fun Route.authRoutes() {
    route("/auth") {
        post("/login") {
            val authRequest = call.receive<AuthRequest>()
            val username = authRequest.username


            if (BruteForceProtectionService.isUserBlocked(username)) {
                val minutesLeft = BruteForceProtectionService.getBlockTimeRemaining(username)
                call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf(
                        "error" to "Account temporarily locked",
                        "message" to "Too many failed login attempts. Try again in $minutesLeft minutes.",
                        "retryAfter" to minutesLeft
                    )
                )
                return@post
            }

            val isValid = UserRepository.validateUser(username, authRequest.password)

            // Записываем попытку входа
            BruteForceProtectionService.recordLoginAttempt(username, isValid)

            if (isValid) {
                val token = JwtService.generateToken(username)
                call.respond(AuthResponse(token))
            } else {
                val failedAttempts = BruteForceProtectionService.loginAttempts[username]
                    ?.count { !it.successful } ?: 0
                val remainingAttempts = 3 - failedAttempts

                val response = if (remainingAttempts > 0) {
                    mapOf(
                        "error" to "Invalid credentials",
                        "message" to "Invalid username or password",
                        "remainingAttempts" to remainingAttempts
                    )
                } else {
                    mapOf(
                        "error" to "Account locked",
                        "message" to "Too many failed attempts. Account locked for 15 minutes.",
                        "retryAfter" to 15
                    )
                }

                call.respond(
                    HttpStatusCode.Unauthorized,
                    response
                )
            }
        }
    }
}