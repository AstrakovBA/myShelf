package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.AuthResponse;
import com.myshelf.wardrobe.dto.PasswordChangeDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.dto.UserRegistrationDTO;
import com.myshelf.wardrobe.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер для управления аутентификацией и аккаунтом пользователя.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Создаёт контроллер аутентификации.
     *
     * @param authService сервис регистрации и входа
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Регистрирует нового пользователя и возвращает JWT с профилем.
     *
     * @param request данные регистрации
     * @return токен и профиль пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDTO request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Выполняет вход по email и паролю.
     *
     * @param email email пользователя
     * @param password пароль
     * @return токен и профиль пользователя
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestParam String email,
                                              @RequestParam String password) {
        AuthResponse response = authService.login(email, password);
        return ResponseEntity.ok(response);
    }

    /**
     * Меняет пароль текущего авторизованного пользователя.
     *
     * @param dto текущий и новый пароль
     * @param authentication контекст аутентификации
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto,
                                               Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.changePassword(userId, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает профиль текущего авторизованного пользователя.
     *
     * @param authentication контекст аутентификации
     * @return профиль пользователя
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserProfileDTO profile = authService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }
}
