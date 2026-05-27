package com.myshelf.wardrobe.dto;

import com.myshelf.wardrobe.entity.Category;
import com.myshelf.wardrobe.entity.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO для вещи (предмета гардероба).
 */
public class ItemDTO {
    private UUID id;

    @NotBlank(message = "Название вещи не может быть пустым")
    private String name;

    private String description;

    private String imageUrl;

    @NotNull(message = "Категория обязательна")
    private Category category;

    private Season season;

    public ItemDTO() {
    }

    public ItemDTO(UUID id, String name, String description, String imageUrl, Category category, Season season) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.season = season;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }
}
