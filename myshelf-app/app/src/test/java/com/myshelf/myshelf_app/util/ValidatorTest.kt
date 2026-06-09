package com.myshelf.myshelf_app.util

import com.myshelf.myshelf_app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {

    @Test
    fun `isValidEmail returns true for valid email`() {
        assertTrue(Validator.isValidEmail("user@example.com"))
        assertTrue(Validator.isValidEmail("  user.name+tag@domain.co.uk  "))
    }

    @Test
    fun `isValidEmail returns false for invalid email`() {
        assertFalse(Validator.isValidEmail(""))
        assertFalse(Validator.isValidEmail("not-an-email"))
        assertFalse(Validator.isValidEmail("@missing-local.com"))
    }

    @Test
    fun `isValidPassword returns false for short password`() {
        assertFalse(Validator.isValidPassword("short"))
        assertFalse(Validator.isValidPassword("1234567"))
    }

    @Test
    fun `isValidPassword returns true for password with at least 8 characters`() {
        assertTrue(Validator.isValidPassword("12345678"))
        assertTrue(Validator.isValidPassword("secure-password"))
    }

    @Test
    fun `validateRegisterInput returns errors for invalid fields`() {
        val errors = Validator.validateRegisterInput(
            email = "bad-email",
            password = "123",
            displayName = ""
        )

        assertEquals(3, errors.size)
        assertTrue(errors.any { it.field == AuthField.DISPLAY_NAME })
        assertTrue(errors.any { it.field == AuthField.EMAIL })
        assertTrue(errors.any {
            it.field == AuthField.PASSWORD && it.messageRes == R.string.error_password_too_short
        })
    }

    @Test
    fun `validateLoginInput returns no errors for valid credentials`() {
        val errors = Validator.validateLoginInput(
            email = "user@example.com",
            password = "password123"
        )

        assertTrue(errors.isEmpty())
    }
}
