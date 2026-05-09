package com.wardrobe.user;

import com.wardrobe.user.ProfileRequests.CreateProfileRequest;
import com.wardrobe.user.ProfileRequests.UpdateProfileRequest;
import com.wardrobe.user.UserResponses.ProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Profiles", description = "CRUD operations for user profiles")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get the current user's profile")
    @GetMapping("/profile")
    public ProfileDto getProfile(Authentication authentication) {
        return profileService.getCurrentProfile(authentication);
    }

    @Operation(summary = "Update the current user's profile")
    @PutMapping("/profile")
    public ProfileDto updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateCurrentProfile(authentication, request);
    }

    @Operation(summary = "Create a profile for a user")
    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileDto createProfile(@Valid @RequestBody CreateProfileRequest request) {
        return profileService.createProfile(request);
    }

    @Operation(summary = "Get all profiles")
    @GetMapping("/profiles")
    public List<ProfileDto> getAllProfiles() {
        return profileService.getAllProfiles();
    }

    @Operation(summary = "Get a profile by ID")
    @GetMapping("/profiles/{id}")
    public ProfileDto getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id);
    }

    @Operation(summary = "Update a profile by ID")
    @PutMapping("/profiles/{id}")
    public ProfileDto updateProfileById(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(id, request);
    }

    @Operation(summary = "Delete a profile by ID")
    @DeleteMapping("/profiles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
    }
}
