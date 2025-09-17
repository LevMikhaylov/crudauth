package com.example

import com.example.routing.authRoutes
import com.example.routing.carRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting() {
    routing {
        authRoutes()
        carRoutes()
        // Корневой маршрут
        get("/") {
            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "message" to "User Management API",
                    "endpoints" to listOf(
                        "GET /users - Get all users",
                        "GET /users/{id} - Get user by ID",
                        "POST /users - Create new user",
                        "DELETE /users/{id} - Delete user"
                    )
                )
            )
        }

        // GET /users/{id} - получить пользователя по ID
        get("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid user ID")

            val user = users.find { it.id == id }
                ?: throw NotFoundException("User with ID $id not found")

            call.respond(HttpStatusCode.OK, user)
        }

        // POST /users - создать нового пользователя
        post("/users") {
            val user = call.receive<User>()

            // Валидация
            if (user.username.isBlank() || user.password.isBlank()) {
                throw IllegalArgumentException("Username and password are required")
            }

            if (users.any { it.username == user.username }) {
                throw ConflictException("User with this username already exists")
            }

            // Генерация ID
            val newId = (users.maxOfOrNull { it.id } ?: 0) + 1
            val newUser = user.copy(id = newId)
            users.add(newUser)

            call.respond(HttpStatusCode.Created, newUser)
        }

        // DELETE /users/{id} - удалить пользователя
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw IllegalArgumentException("Invalid user ID")

            val user = users.find { it.id == id }
                ?: throw NotFoundException("User with ID $id not found")

            users.remove(user)
            call.respond(HttpStatusCode.OK, mapOf("message" to "User deleted successfully"))
        }


    }
}
class NotFoundException(message: String) : Exception(message)
class ConflictException(message: String) : Exception(message)