package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.AuthResponse;
import com.myshelf.wardrobe.dto.PasswordChangeDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.dto.UserRegistrationDTO;
import com.myshelf.wardrobe.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Контроллер для управления аутентификацией и аккаунтом пользователя.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрация нового пользователя.
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDTO request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Аутентификация пользователя (вход).
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestParam String email,
                                              @RequestParam String password) {
        AuthResponse response = authService.login(email, password);
        return ResponseEntity.ok(response);
    }

    /**
     * Смена пароля авторизованным пользователем.
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto,
                                               Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.changePassword(userId, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Получение профиля текущего пользователя.
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserProfileDTO profile = authService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
