package com.wardrobe.user;

import com.wardrobe.user.ProfileRequests.CreateProfileRequest;
import com.wardrobe.user.ProfileRequests.UpdateProfileRequest;
import com.wardrobe.user.UserResponses.ProfileDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final UserService userService;

    public ProfileService(ProfileRepository profileRepository, UserService userService) {
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @Transactional
    public ProfileDto createProfile(CreateProfileRequest request) {
        AppUser user = userService.findUserById(request.userId());
        profileRepository.findByUser(user).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has a profile");
        });

        Profile profile = new Profile(user, request.displayName().trim());
        profile.setBio(cleanBio(request.bio()));
        user.setProfile(profile);
        return ProfileDto.from(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public List<ProfileDto> getAllProfiles() {
        return profileRepository.findAll().stream()
                .map(ProfileDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfileById(Long id) {
        return ProfileDto.from(findProfileById(id));
    }

    @Transactional(readOnly = true)
    public ProfileDto getCurrentProfile(Authentication authentication) {
        AppUser user = userService.currentUser(authentication);
        return ProfileDto.from(findProfileByUser(user));
    }

    @Transactional
    public ProfileDto updateProfile(Long id, UpdateProfileRequest request) {
        Profile profile = findProfileById(id);
        applyProfileUpdate(profile, request);
        return ProfileDto.from(profileRepository.save(profile));
    }

    @Transactional
    public ProfileDto updateCurrentProfile(Authentication authentication, UpdateProfileRequest request) {
        AppUser user = userService.currentUser(authentication);
        Profile profile = findProfileByUser(user);
        applyProfileUpdate(profile, request);
        return ProfileDto.from(profileRepository.save(profile));
    }

    @Transactional
    public void deleteProfile(Long id) {
        Profile profile = findProfileById(id);
        profileRepository.delete(profile);
    }

    private Profile findProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private Profile findProfileByUser(AppUser user) {
        return profileRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private void applyProfileUpdate(Profile profile, UpdateProfileRequest request) {
        profile.setDisplayName(request.displayName().trim());
        profile.setBio(cleanBio(request.bio()));
    }

    private String cleanBio(String bio) {
        return bio == null ? null : bio.trim();
    }
}
