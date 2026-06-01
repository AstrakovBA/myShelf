package com.myshelf.wardrobe.mapper;

import com.myshelf.wardrobe.dto.ItemDTO;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Маппер для конвертации между {@link Item} и {@link ItemDTO}.
 */
@Component
public class ItemMapper {

    /**
     * Создаёт сущность вещи из DTO с указанным владельцем.
     *
     * @param dto данные вещи
     * @param owner пользователь-владелец
     * @return новая сущность Item
     */
    public Item toEntity(ItemDTO dto, User owner) {
        return Item.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .category(dto.getCategory())
                .season(dto.getSeason())
                .build();
    }

    /**
     * Преобразует сущность вещи в DTO.
     *
     * @param entity сущность Item
     * @return DTO вещи
     */
    public ItemDTO toDTO(Item entity) {
        return new ItemDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getImageUrl(),
                entity.getCategory(),
                entity.getSeason()
        );
    }

    /**
     * Обновляет поля сущности из DTO.
     *
     * @param entity обновляемая сущность
     * @param dto источник данных
     */
    public void updateEntityFromDTO(Item entity, ItemDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setImageUrl(dto.getImageUrl());
        entity.setCategory(dto.getCategory());
        entity.setSeason(dto.getSeason());
    }
}
