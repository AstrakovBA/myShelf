package com.myshelf.myshelf_app.util

import androidx.annotation.StringRes
import com.myshelf.myshelf_app.R
import java.util.regex.Pattern

enum class AuthField {
    DISPLAY_NAME,
    EMAIL,
    PASSWORD
}

data class AuthValidationError(
    val field: AuthField,
    @StringRes val messageRes: Int
)

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
    ): List<AuthValidationError> {
        val errors = mutableListOf<AuthValidationError>()

        if (displayName.isBlank()) {
            errors.add(AuthValidationError(AuthField.DISPLAY_NAME, R.string.error_display_name_required))
        }
        if (!isValidEmail(email)) {
            errors.add(AuthValidationError(AuthField.EMAIL, R.string.error_invalid_email))
        }
        if (password.isBlank()) {
            errors.add(AuthValidationError(AuthField.PASSWORD, R.string.error_password_empty))
        } else if (!isValidPassword(password)) {
            errors.add(AuthValidationError(AuthField.PASSWORD, R.string.error_password_too_short))
        }

        return errors
    }

    fun validateLoginInput(email: String, password: String): List<AuthValidationError> {
        val errors = mutableListOf<AuthValidationError>()

        if (!isValidEmail(email)) {
            errors.add(AuthValidationError(AuthField.EMAIL, R.string.error_invalid_email))
        }
        if (password.isBlank()) {
            errors.add(AuthValidationError(AuthField.PASSWORD, R.string.error_password_empty))
        }

        return errors
    }
}
