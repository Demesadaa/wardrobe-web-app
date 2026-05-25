package com.wardrobe.user;

import com.wardrobe.user.UserRequests.RegisterRequest;
import com.wardrobe.user.UserRequests.UpdateUserRequest;
import com.wardrobe.user.UserResponses.UserDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDto createUser(RegisterRequest request) {
        ensureUsernameAvailable(request.username(), null);
        ensureEmailAvailable(request.email(), null);

        AppUser user = new AppUser(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password()));
        Profile profile = new Profile(user, request.username().trim());
        user.setProfile(profile);

        return UserDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        return UserDto.from(findUserById(id));
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        AppUser user = findUserById(id);
        ensureUsernameAvailable(request.username(), id);
        ensureEmailAvailable(request.email(), id);

        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        AppUser user = findUserById(id);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public AppUser currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    @Transactional(readOnly = true)
    public UserDto currentUserDto(Authentication authentication) {
        return UserDto.from(currentUser(authentication));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        return UserDto.from(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
    }

    AppUser findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void ensureUsernameAvailable(String username, Long currentUserId) {
        userRepository.findByUsername(username.trim())
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
                });
    }

    private void ensureEmailAvailable(String email, Long currentUserId) {
        String normalizedEmail = email.trim().toLowerCase();
        userRepository.findByEmail(normalizedEmail)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
                });
    }
}
