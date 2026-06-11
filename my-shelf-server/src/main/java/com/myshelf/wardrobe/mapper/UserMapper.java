package com.myshelf.wardrobe.mapper;

import com.myshelf.wardrobe.dto.UpdateProfileDTO;
import com.myshelf.wardrobe.dto.UserProfileDTO;
import com.myshelf.wardrobe.entity.User;
import org.springframework.stereotype.Component;

/**
 * Маппер для конвертации между {@link User} и {@link UserProfileDTO}.
 */
@Component
public class UserMapper {

    /**
     * Преобразует сущность пользователя в DTO профиля.
     *
     * @param entity сущность User
     * @return DTO профиля
     */
    public UserProfileDTO toDTO(User entity) {
        return new UserProfileDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getAvatarUrl()
        );
    }

    /**
     * Обновляет отображаемые поля пользователя из DTO профиля.
     *
     * @param entity обновляемая сущность
     * @param dto источник данных
     */
    public void updateEntityFromDTO(User entity, UserProfileDTO dto) {
        entity.setDisplayName(dto.getDisplayName());
        entity.setAvatarUrl(dto.getAvatarUrl());
    }

    /**
     * Применяет к сущности только переданные поля обновления профиля.
     *
     * @param entity обновляемая сущность
     * @param dto источник данных
     */
    public void updateEntityFromDTO(User entity, UpdateProfileDTO dto) {
        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
        if (dto.getAvatarUrl() != null) {
            entity.setAvatarUrl(dto.getAvatarUrl());
        }
    }
}
