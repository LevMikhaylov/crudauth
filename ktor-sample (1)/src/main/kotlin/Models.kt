package com.example

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val password: String,
)

@Serializable
data class ErrorResponse(
    val message: String,
    val code: Int
)

val users = mutableListOf(
    User(1, "Иван Иванов",  "ffggddfd25"),
    User(2, "Мария Петрова",  "sdsdsds232"),
    User(3, "Алексей Сидоров",  "34343dsd")
)
@Serializable
data class Car(
    val id: Int? = null,
    val brand: String,
    val model: String,
    val licensePlate: String
)

@Serializable
data class CarResponse(
    val success: Boolean,
    val message: String? = null,
    val car: Car? = null,
    val cars: List<Car>? = null
)
@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String
)