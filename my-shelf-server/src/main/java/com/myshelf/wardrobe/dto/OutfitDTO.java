package com.myshelf.wardrobe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * DTO для образа (комплекта вещей).
 */
public class OutfitDTO {
    private UUID id;

    @NotBlank(message = "Название образа не может быть пустым")
    private String name;

    private String description;

    @NotEmpty(message = "Образ должен содержать хотя бы один слот")
    private List<OutfitSlotDTO> slots;

    public OutfitDTO() {
    }

    public OutfitDTO(UUID id, String name, String description, List<OutfitSlotDTO> slots) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slots = slots;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OutfitSlotDTO> getSlots() {
        return slots;
    }

    public void setSlots(List<OutfitSlotDTO> slots) {
        this.slots = slots;
    }
}
