package com.myshelf.wardrobe.dto;

import com.myshelf.wardrobe.entity.Category;

import java.util.UUID;

/**
 * DTO для слота образа.
 */
public class OutfitSlotDTO {
    private UUID id;
    private UUID itemId;
    private Category slotType;

    public OutfitSlotDTO() {
    }

    public OutfitSlotDTO(UUID id, UUID itemId, Category slotType) {
        this.id = id;
        this.itemId = itemId;
        this.slotType = slotType;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public Category getSlotType() {
        return slotType;
    }

    public void setSlotType(Category slotType) {
        this.slotType = slotType;
    }
}
