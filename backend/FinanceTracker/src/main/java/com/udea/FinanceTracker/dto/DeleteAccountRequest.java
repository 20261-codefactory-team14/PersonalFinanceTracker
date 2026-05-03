package com.udea.FinanceTracker.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for account deletion request
 * Requires user confirmation to delete their account
 */
public class DeleteAccountRequest {

    @NotNull(message = "La confirmación es requerida")
    private Boolean confirm;

    public DeleteAccountRequest() {}

    public DeleteAccountRequest(Boolean confirm) {
        this.confirm = confirm;
    }

    public Boolean getConfirm() {
        return confirm;
    }

    public void setConfirm(Boolean confirm) {
        this.confirm = confirm;
    }

    @Override
    public String toString() {
        return "DeleteAccountRequest{" +
                "confirm=" + confirm +
                '}';
    }
}

