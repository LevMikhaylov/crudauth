package com.example.Services

import com.example.LoginAttempt
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

object BruteForceProtectionService {
    private const val MAX_ATTEMPTS = 3
    private const val BLOCK_DURATION_MINUTES = 15

   val loginAttempts = ConcurrentHashMap<String, MutableList<LoginAttempt>>()
    val blockedUsers = ConcurrentHashMap<String, LocalDateTime>()

    fun recordLoginAttempt(username: String, successful: Boolean) {
        val attempt = LoginAttempt(username, LocalDateTime.now(), successful)

        loginAttempts.compute(username) { _, attempts ->
            val list = attempts ?: mutableListOf()
            list.add(attempt)
            // Храним только последние MAX_ATTEMPTS * 2 попыток
            if (list.size > MAX_ATTEMPTS * 2) {
                list.subList(0, list.size - MAX_ATTEMPTS).clear()
            }
            list
        }

        if (!successful) {
            checkAndBlockUser(username)
        } else {
            // При успешном входе разблокируем пользователя
            blockedUsers.remove(username)
        }
    }

    private fun checkAndBlockUser(username: String) {
        val attempts = loginAttempts[username] ?: return
        val recentFailedAttempts = attempts
            .filter { !it.successful }
            .filter { it.timestamp.isAfter(LocalDateTime.now().minusMinutes(BLOCK_DURATION_MINUTES.toLong())) }

        if (recentFailedAttempts.size >= MAX_ATTEMPTS) {
            blockedUsers[username] = LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES.toLong())
        }
    }

    fun isUserBlocked(username: String): Boolean {
        val blockUntil = blockedUsers[username] ?: return false

        if (blockUntil.isBefore(LocalDateTime.now())) {
            blockedUsers.remove(username)
            return false
        }

        return true
    }

    fun getBlockTimeRemaining(username: String): Long {
        val blockUntil = blockedUsers[username] ?: return 0
        val now = LocalDateTime.now()

        return if (blockUntil.isAfter(now)) {
            java.time.Duration.between(now, blockUntil).toMinutes()
        } else {
            0
        }
    }

    fun cleanupOldData() {
        val cutoff = LocalDateTime.now().minusHours(24)

        // Очищаем старые попытки входа
        loginAttempts.forEach { (username, attempts) ->
            attempts.removeAll { it.timestamp.isBefore(cutoff) }
            if (attempts.isEmpty()) {
                loginAttempts.remove(username)
            }
        }

        // Очищаем старые блокировки
        blockedUsers.entries.removeAll { it.value.isBefore(LocalDateTime.now()) }
    }
}