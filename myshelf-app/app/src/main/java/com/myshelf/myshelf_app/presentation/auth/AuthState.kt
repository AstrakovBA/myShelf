package com.myshelf.myshelf_app.presentation.auth

sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Guest(val guestId: String) : AuthState()
    data object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
