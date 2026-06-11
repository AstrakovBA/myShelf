package com.myshelf.wardrobe.service;

import com.myshelf.wardrobe.dto.AuthResponse;
import com.myshelf.wardrobe.dto.PasswordChangeDTO;
import com.myshelf.wardrobe.dto.PasswordConfirmRequest;
import com.myshelf.wardrobe.dto.UpdateProfileDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.dto.UserRegistrationDTO;
import jakarta.persistence.EntityNotFoundException;
import com.myshelf.wardrobe.entity.User;
import com.myshelf.wardrobe.entity.UserSettings;
import com.myshelf.wardrobe.mapper.UserMapper;
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
    private final UserMapper userMapper;

    /**
     * Создаёт сервис аутентификации.
     *
     * @param userRepository репозиторий пользователей
     * @param userSettingsRepository репозиторий настроек
     * @param passwordEncoder кодировщик паролей
     * @param jwtTokenProvider провайдер JWT
     * @param userMapper маппер профиля
     */
    public AuthService(UserRepository userRepository,
                       UserSettingsRepository userSettingsRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }

    /**
     * Регистрирует нового пользователя и выдаёт JWT.
     *
     * @param registrationDTO email, пароль и отображаемое имя
     * @return токен и профиль пользователя
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
        UserProfileDTO profile = userMapper.toDTO(savedUser);

        return new AuthResponse(token, profile);
    }

    /**
     * Аутентифицирует пользователя по email и паролю.
     *
     * @param email email пользователя
     * @param password пароль в открытом виде
     * @return токен и профиль пользователя
     */
    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }

        String token = jwtTokenProvider.generateToken(user.getId());
        UserProfileDTO profile = userMapper.toDTO(user);

        return new AuthResponse(token, profile);
    }

    /**
     * Меняет пароль пользователя после проверки текущего.
     *
     * @param userId идентификатор пользователя
     * @param passwordChangeDTO текущий и новый пароль
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
     * Возвращает профиль пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return DTO профиля
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return userMapper.toDTO(user);
    }

    /**
     * Обновляет отображаемое имя и аватар пользователя.
     *
     * @param userId идентификатор пользователя
     * @param dto новые данные профиля
     * @return обновлённый профиль
     */
    public UserProfileDTO updateProfile(UUID userId, UpdateProfileDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        userMapper.updateEntityFromDTO(user, dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDTO(savedUser);
    }

    /**
     * Удаляет учётную запись после проверки текущего пароля.
     *
     * @param userId идентификатор пользователя
     * @param request подтверждение паролем
     */
    public void deleteAccount(UUID userId, PasswordConfirmRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        userRepository.deleteById(userId);
    }
}
