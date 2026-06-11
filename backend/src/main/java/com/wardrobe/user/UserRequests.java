package com.wardrobe.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class UserRequests {
    private UserRequests() {
    }

    public record RegisterRequest(
            @NotBlank(message = "{validation.username.required}")
            @Size(min = 3, max = 40, message = "{validation.username.size}")
            String username,
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Size(max = 120, message = "{validation.email.size}")
            String email,
            @NotBlank(message = "{validation.password.required}")
            @Size(min = 8, max = 100, message = "{validation.password.size}")
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "{validation.username.required}") String username,
            @NotBlank(message = "{validation.password.required}") String password) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "{validation.username.required}")
            @Size(min = 3, max = 40, message = "{validation.username.size}")
            String username,
            @NotBlank(message = "{validation.email.required}")
            @Email(message = "{validation.email.invalid}")
            @Size(max = 120, message = "{validation.email.size}")
            String email,
            @Size(min = 8, max = 100, message = "{validation.password.size}")
            String password) {
    }
}
