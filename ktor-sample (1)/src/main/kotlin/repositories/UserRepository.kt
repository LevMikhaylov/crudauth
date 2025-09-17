package com.example.repositories

import com.example.User
import com.example.Users
import com.example.security.PasswordHasher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {

    fun createUser(username: String, password: String): User = transaction {
        val hashedPassword = PasswordHasher.hashPassword(password)

        val insertStatement = Users.insert {
            it[Users.username] = username
            it[Users.password] = hashedPassword
        }

        val id = insertStatement[Users.id]
        User(id = id, username = username, password = hashedPassword)
    }

    fun findByUsername(username: String): User? = transaction {
        val query = Users.select { Users.username eq username }
        query.singleOrNull()?.let { row ->
            User(
                id = row[Users.id],
                username = row[Users.username],
                password = row[Users.password]
            )
        }
    }

    fun validateUser(username: String, password: String): Boolean {
        val user = findByUsername(username)
        return user != null && PasswordHasher.verifyPassword(password, user.password)
    }
}