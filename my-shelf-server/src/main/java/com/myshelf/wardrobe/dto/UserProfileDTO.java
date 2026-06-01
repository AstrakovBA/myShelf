package com.myshelf.wardrobe.dto;

import java.util.UUID;

/**
 * DTO для отображения профиля пользователя.
 */
public class UserProfileDTO {
    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;

    public UserProfileDTO() {
    }

    public UserProfileDTO(UUID id, String email, String displayName, String avatarUrl) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
