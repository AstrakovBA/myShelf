package com.myshelf.wardrobe.mapper;

import com.myshelf.wardrobe.dto.OutfitDTO;
import com.myshelf.wardrobe.dto.OutfitSlotDTO;
import com.myshelf.wardrobe.entity.Item;
import com.myshelf.wardrobe.entity.Outfit;
import com.myshelf.wardrobe.entity.OutfitSlot;
import com.myshelf.wardrobe.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер для конвертации между {@link Outfit} и {@link OutfitDTO}.
 */
@Component
public class OutfitMapper {

    /**
     * Создаёт сущность образа из DTO с указанным владельцем.
     *
     * @param dto данные образа
     * @param owner пользователь-владелец
     * @return новая сущность Outfit
     */
    public Outfit toEntity(OutfitDTO dto, User owner) {
        return Outfit.builder()
                .id(UUID.randomUUID())
                .user(owner)
                .name(dto.getName())
                .description(dto.getDescription())
                .season(dto.getSeason())
                .slots(new ArrayList<>())
                .build();
    }

    /**
     * Преобразует сущность образа в DTO со слотами.
     *
     * @param entity сущность Outfit
     * @return DTO образа
     */
    public OutfitDTO toDTO(Outfit entity) {
        List<OutfitSlotDTO> slotDTOs = entity.getSlots().stream()
                .map(this::toSlotDTO)
                .collect(Collectors.toList());

        return new OutfitDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getSeason(),
                slotDTOs
        );
    }

    /**
     * Обновляет поля сущности образа из DTO.
     *
     * @param entity обновляемая сущность
     * @param dto источник данных
     */
    public void updateEntityFromDTO(Outfit entity, OutfitDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSeason(dto.getSeason());
    }

    /**
     * Создаёт сущность слота образа.
     *
     * @param dto данные слота
     * @param outfit родительский образ
     * @param item вещь в слоте или {@code null}
     * @return новая сущность OutfitSlot
     */
    public OutfitSlot toSlotEntity(OutfitSlotDTO dto, Outfit outfit, Item item) {
        return OutfitSlot.builder()
                .id(UUID.randomUUID())
                .outfit(outfit)
                .item(item)
                .slotType(dto.getSlotType())
                .build();
    }

    /**
     * Преобразует сущность слота в DTO.
     *
     * @param slot сущность OutfitSlot
     * @return DTO слота
     */
    public OutfitSlotDTO toSlotDTO(OutfitSlot slot) {
        UUID itemId = slot.getItem() != null ? slot.getItem().getId() : null;
        return new OutfitSlotDTO(
                slot.getId(),
                itemId,
                slot.getSlotType()
        );
    }
}
