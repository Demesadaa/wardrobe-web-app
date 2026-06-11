package com.wardrobe.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DemoUserDataInitializer {
    @Bean
    CommandLineRunner seedAssignmentUsers(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(
                    userRepository,
                    passwordEncoder,
                    "admin",
                    "admin@wardrobe.local",
                    "Admin123!",
                    UserRole.ADMIN,
                    "Admin Demetre");
            seedUser(
                    userRepository,
                    passwordEncoder,
                    "demetre",
                    "demetre@wardrobe.local",
                    "User12345!",
                    UserRole.USER,
                    "Demetre");
        };
    }

    private void seedUser(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String email,
            String password,
            UserRole role,
            String displayName) {
        AppUser user = userRepository.findByUsername(username)
                .orElseGet(() -> new AppUser(username, email, passwordEncoder.encode(password), role));

        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        if (user.getProfile() == null) {
            user.setProfile(new Profile(user, displayName));
        }

        userRepository.save(user);
    }
}
