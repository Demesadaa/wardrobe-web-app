package com.wardrobe.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserRequests {
    private UserRequests() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 40) String username,
            @NotBlank @Email @Size(max = 120) String email,
            @NotBlank @Size(min = 8, max = 100) String password) {
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record UpdateUserRequest(
            @NotBlank @Size(min = 3, max = 40) String username,
            @NotBlank @Email @Size(max = 120) String email,
            @Size(min = 8, max = 100) String password) {
    }
}
