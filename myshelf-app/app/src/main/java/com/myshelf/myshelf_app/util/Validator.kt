package com.myshelf.myshelf_app.util

import java.util.regex.Pattern

object Validator {

    private val EMAIL_PATTERN: Pattern = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && EMAIL_PATTERN.matcher(trimmed).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    fun validateRegisterInput(
        email: String,
        password: String,
        displayName: String
    ): List<String> {
        val errors = mutableListOf<String>()

        if (displayName.isBlank()) {
            errors.add("Укажите имя пользователя")
        }
        if (!isValidEmail(email)) {
            errors.add("Некорректный формат email")
        }
        if (password.isBlank()) {
            errors.add("Пароль не может быть пустым")
        } else if (!isValidPassword(password)) {
            errors.add("Пароль должен содержать минимум 8 символов")
        }

        return errors
    }

    fun validateLoginInput(email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        if (!isValidEmail(email)) {
            errors.add("Некорректный формат email")
        }
        if (password.isBlank()) {
            errors.add("Пароль не может быть пустым")
        }

        return errors
    }
}
