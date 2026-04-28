package com.wardrobe.user;

public final class UserResponses {
    private UserResponses() {
    }

    public record UserDto(Long id, String username, String email, String displayName) {
        public static UserDto from(AppUser user) {
            String displayName = user.getProfile() == null ? user.getUsername() : user.getProfile().getDisplayName();
            return new UserDto(user.getId(), user.getUsername(), user.getEmail(), displayName);
        }
    }

    public record ProfileDto(Long id, String username, String email, String displayName, String bio) {
        public static ProfileDto from(Profile profile) {
            AppUser user = profile.getUser();
            return new ProfileDto(
                    profile.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    profile.getDisplayName(),
                    profile.getBio());
        }
    }
}
