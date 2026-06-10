package com.myshelf.wardrobe.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO для подтверждения действия текущим паролем (удаление аккаунта).
 */
public class PasswordConfirmRequest {

    @NotBlank(message = "Текущий пароль не может быть пустым")
    private String currentPassword;

    public PasswordConfirmRequest() {
    }

    public PasswordConfirmRequest(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
}
