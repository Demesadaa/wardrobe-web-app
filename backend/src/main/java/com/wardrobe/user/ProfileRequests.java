package com.wardrobe.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ProfileRequests {
    private ProfileRequests() {
    }

    public record CreateProfileRequest(
            @NotNull(message = "{validation.profile.userId.required}") Long userId,
            @NotBlank(message = "{validation.profile.displayName.required}")
            @Size(max = 80, message = "{validation.profile.displayName.size}")
            String displayName,
            @Size(max = 280, message = "{validation.profile.bio.size}") String bio) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "{validation.profile.displayName.required}")
            @Size(max = 80, message = "{validation.profile.displayName.size}")
            String displayName,
            @Size(max = 280, message = "{validation.profile.bio.size}") String bio) {
    }
}
