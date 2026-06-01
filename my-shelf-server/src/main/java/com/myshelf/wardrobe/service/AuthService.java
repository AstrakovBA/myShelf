package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.AuthResponse;
import com.myshelf.wardrobe.dto.PasswordChangeDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.dto.UserRegistrationDTO;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.entity.UserSettings;
import com.myshelf.wardrobe.repository.UserRepository;
import com.myshelf.wardrobe.repository.UserSettingsRepository;
import com.myshelf.wardrobe.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Сервис для управления аутентификацией и регистрацией пользователей.
 */
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository,
                       UserSettingsRepository userSettingsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Регистрация нового пользователя.
     */
    public AuthResponse register(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(registrationDTO.getEmail())
                .passwordHash(passwordEncoder.encode(registrationDTO.getPassword()))
                .displayName(registrationDTO.getDisplayName())
                .build();

        User savedUser = userRepository.save(user);

        // Создаем настройки пользователя по умолчанию
        UserSettings settings = UserSettings.builder()
                .userId(UUID.randomUUID())
                .user(savedUser)
                .build();
        userSettingsRepository.save(settings);

        String token = jwtTokenProvider.generateToken(savedUser.getId());
        UserProfileDTO profile = convertToProfileDTO(savedUser);

        return new AuthResponse(token, profile);
    }

    /**
     * Аутентификация пользователя (вход).
     */
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }

        String token = jwtTokenProvider.generateToken(user.getId());
        UserProfileDTO profile = convertToProfileDTO(user);

        return new AuthResponse(token, profile);
    }

    /**
     * Смена пароля пользователя.
     */
    public void changePassword(UUID userId, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Текущий пароль неверен");
        }

        user.setPasswordHash(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Получение профиля пользователя.
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return convertToProfileDTO(user);
    }

    private UserProfileDTO convertToProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl()
        );
    }
}
