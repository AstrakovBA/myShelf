package com.myshelf.wardrobe.controller;

import com.myshelf.wardrobe.dto.PasswordConfirmRequest;
import com.myshelf.wardrobe.dto.UpdateProfileDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер для управления учётной записью пользователя.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AuthService authService;

    /**
     * Создаёт контроллер пользовательских операций.
     *
     * @param authService сервис аутентификации и аккаунта
     */
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Обновляет публичные данные профиля текущего пользователя.
     *
     * @param dto данные для обновления
     * @param authentication контекст аутентификации
     * @return актуальный профиль
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @Valid @RequestBody UpdateProfileDTO dto,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserProfileDTO profile = authService.updateProfile(userId, dto);
        return ResponseEntity.ok(profile);
    }

    /**
     * Удаляет аккаунт текущего пользователя после подтверждения паролем.
     *
     * @param request текущий пароль
     * @param authentication контекст аутентификации
     */
    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount(
            @Valid @RequestBody PasswordConfirmRequest request,
            Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        authService.deleteAccount(userId, request);
        return ResponseEntity.noContent().build();
    }
}
