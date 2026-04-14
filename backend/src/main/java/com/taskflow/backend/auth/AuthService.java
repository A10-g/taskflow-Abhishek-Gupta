package com.taskflow.backend.auth;

import com.taskflow.backend.auth.dto.AuthResponse;
import com.taskflow.backend.auth.dto.LoginRequest;
import com.taskflow.backend.auth.dto.RegisterRequest;
import com.taskflow.backend.auth.security.CurrentUser;
import com.taskflow.backend.auth.security.CurrentUserDetailsService;
import com.taskflow.backend.auth.security.JwtService;
import com.taskflow.backend.common.exception.ConflictException;
import com.taskflow.backend.common.exception.UnauthorizedException;
import com.taskflow.backend.user.model.UserAccount;
import com.taskflow.backend.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserDetailsService currentUserDetailsService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CurrentUserDetailsService currentUserDetailsService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserDetailsService = currentUserDetailsService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("email already exists");
        }

        UserAccount user = new UserAccount();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));

        UserAccount savedUser = userRepository.save(user);
        CurrentUser currentUser = currentUserDetailsService.loadCurrentUser(savedUser);
        return toAuthResponse(currentUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("invalid credentials");
        }

        CurrentUser currentUser = currentUserDetailsService.loadCurrentUser(user);
        return toAuthResponse(currentUser);
    }

    private AuthResponse toAuthResponse(CurrentUser currentUser) {
        return new AuthResponse(
                jwtService.generateToken(currentUser),
                new AuthResponse.AuthUser(currentUser.id(), currentUser.name(), currentUser.email())
        );
    }
}
