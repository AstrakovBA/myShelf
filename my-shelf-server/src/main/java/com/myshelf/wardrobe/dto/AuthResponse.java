package com.myshelf.wardrobe.dto;

/**
 * DTO для ответа при аутентификации и регистрации.
 */
public class AuthResponse {
    private String token;
    private UserProfileDTO profile;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserProfileDTO profile) {
        this.token = token;
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfileDTO getProfile() {
        return profile;
    }

    public void setProfile(UserProfileDTO profile) {
        this.profile = profile;
    }
}
