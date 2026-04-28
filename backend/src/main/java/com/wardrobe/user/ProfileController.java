package com.wardrobe.user;

import com.wardrobe.user.UserResponses.ProfileDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final AuthController authController;
    private final ProfileRepository profileRepository;

    public ProfileController(AuthController authController, ProfileRepository profileRepository) {
        this.authController = authController;
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ProfileDto getProfile(Authentication authentication) {
        return ProfileDto.from(authController.currentProfile(authentication));
    }

    @PutMapping
    public ProfileDto updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        Profile profile = authController.currentProfile(authentication);
        profile.setDisplayName(request.displayName().trim());
        profile.setBio(request.bio() == null ? null : request.bio().trim());
        return ProfileDto.from(profileRepository.save(profile));
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 80) String displayName,
            @Size(max = 280) String bio) {
    }
}
