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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private UUID userId;
    private String testEmail;
    private String testPassword;
    private String testDisplayName;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testEmail = "test@example.com";
        testPassword = "SecurePass123";
        testDisplayName = "TestUser";

        testUser = User.builder()
                .id(userId)
                .email(testEmail)
                .passwordHash("$2a$10$hashedPassword")
                .displayName(testDisplayName)
                .build();
    }

    // ===== REGISTER =====

    @Test
    @DisplayName("register — успешная регистрация нового пользователя")
    void register_success() {
        // Arrange
        UserRegistrationDTO regDTO = new UserRegistrationDTO(testEmail, testPassword, testDisplayName);
        String expectedToken = "jwt.token.here";

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(userId);
            return saved;
        });
        when(jwtTokenProvider.generateToken(userId)).thenReturn(expectedToken);

        // Act
        AuthResponse result = authService.register(regDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(expectedToken);
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getEmail()).isEqualTo(testEmail);
        assertThat(result.getProfile().getDisplayName()).isEqualTo(testDisplayName);

        verify(userRepository).existsByEmail(testEmail);
        verify(passwordEncoder).encode(testPassword);
        verify(userRepository).save(any(User.class));
        verify(userSettingsRepository).save(any(UserSettings.class));
        verify(jwtTokenProvider).generateToken(userId);
    }

    @Test
    @DisplayName("register — email уже занят")
    void register_emailAlreadyExists() {
        // Arrange
        UserRegistrationDTO regDTO = new UserRegistrationDTO(testEmail, testPassword, testDisplayName);
        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(regDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь с таким email уже существует");

        verify(userRepository).existsByEmail(testEmail);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    // ===== LOGIN =====

    @Test
    @DisplayName("login — успешный вход")
    void login_success() {
        // Arrange
        String expectedToken = "jwt.token.here";

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.generateToken(userId)).thenReturn(expectedToken);

        // Act
        AuthResponse result = authService.login(testEmail, testPassword);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(expectedToken);
        assertThat(result.getProfile().getEmail()).isEqualTo(testEmail);

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(testPassword, testUser.getPasswordHash());
        verify(jwtTokenProvider).generateToken(userId);
    }

    @Test
    @DisplayName("login — неверный email (пользователь не найден)")
    void login_userNotFound() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(testEmail, testPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный email или пароль");

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    @DisplayName("login — неверный пароль")
    void login_wrongPassword() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(testPassword, testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(testEmail, testPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Неверный email или пароль");

        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(testPassword, testUser.getPasswordHash());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    // ===== CHANGE PASSWORD =====

    @Test
    @DisplayName("changePassword — успешная смена пароля")
    void changePassword_success() {
        // Arrange
        String currentPassword = "OldPass123";
        String newPassword = "NewPass456";
        PasswordChangeDTO pwdDTO = new PasswordChangeDTO(currentPassword, newPassword);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("$2a$10$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        String oldPasswordHash = testUser.getPasswordHash();

        // Act
        authService.changePassword(userId, pwdDTO);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(currentPassword, oldPasswordHash);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(testUser);

        // Проверяем, что пароль был изменён
        assertThat(testUser.getPasswordHash()).isEqualTo("$2a$10$newHashedPassword");
    }

    @Test
    @DisplayName("changePassword — пользователь не найден")
    void changePassword_userNotFound() {
        // Arrange
        PasswordChangeDTO pwdDTO = new PasswordChangeDTO("old", "new");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(userId, pwdDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    @DisplayName("changePassword — неверный текущий пароль")
    void changePassword_wrongCurrentPassword() {
        // Arrange
        PasswordChangeDTO pwdDTO = new PasswordChangeDTO("wrongOldPass", "NewPass456");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOldPass", testUser.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(userId, pwdDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Текущий пароль неверен");

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches("wrongOldPass", testUser.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    // ===== GET PROFILE =====

    @Test
    @DisplayName("getProfile — успешное получение профиля")
    void getProfile_success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserProfileDTO result = authService.getProfile(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getEmail()).isEqualTo(testEmail);
        assertThat(result.getDisplayName()).isEqualTo(testDisplayName);
        assertThat(result.getAvatarUrl()).isNull();

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("getProfile — пользователь не найден")
    void getProfile_userNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.getProfile(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Пользователь не найден");

        verify(userRepository).findById(userId);
    }
}
