package com.wardrobe.user;

import com.wardrobe.user.UserRequests.LoginRequest;
import com.wardrobe.user.UserRequests.RegisterRequest;
import com.wardrobe.user.UserResponses.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    public UserDto register(RegisterRequest request) {
        log.info("Registering new user with username {}", request.username());
        return userService.createUser(request);
    }

    public UserDto login(LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        log.debug("Attempting login for username {}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, servletRequest, servletResponse);

        log.info("User {} logged in successfully", authentication.getName());
        return userService.getUserByUsername(authentication.getName());
    }

    public void logout(HttpServletRequest request) {
        String sessionId = request.getSession(false) == null ? "none" : request.getSession(false).getId();
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        log.info("User session {} logged out", sessionId);
    }
}
