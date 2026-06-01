package com.myshelf.wardrobe.repository;

import com.myshelf.wardrobe.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для управления сущностями UserSettings.
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, UUID> {

    /**
     * Найти настройки пользователя по ID пользователя.
     */
    Optional<UserSettings> findByUserId(UUID userId);
}
