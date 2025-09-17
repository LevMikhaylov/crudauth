package com.example.Services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtService {
    private const val SECRET = "your-secret-key"
    private const val ISSUER = "your-issuer"
    private const val VALIDITY_MS = 36_000_00 * 10 // 10 hours

    private val algorithm = Algorithm.HMAC256(SECRET)

    fun generateToken(username: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(ISSUER)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
    }

    fun validateToken(token: String): Boolean {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build()
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsernameFromToken(token: String): String? {
        return try {
            val jwt = JWT.decode(token)
            jwt.getClaim("username").asString()
        } catch (e: Exception) {
            null
        }
    }
}