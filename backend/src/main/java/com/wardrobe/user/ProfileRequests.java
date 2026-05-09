package com.wardrobe.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProfileRequests {
    private ProfileRequests() {
    }

    public record CreateProfileRequest(
            @NotNull Long userId,
            @NotBlank @Size(max = 80) String displayName,
            @Size(max = 280) String bio) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 80) String displayName,
            @Size(max = 280) String bio) {
    }
}
