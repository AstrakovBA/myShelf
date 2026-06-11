package com.myshelf.wardrobe.dto;

/**
 * DTO для обновления профиля пользователя.
 */
public class UpdateProfileDTO {

    private String displayName;
    private String avatarUrl;

    public UpdateProfileDTO() {
    }

    public UpdateProfileDTO(String displayName, String avatarUrl) {
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
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
