package com.taskflow.backend.auth.security;

import com.taskflow.backend.user.model.UserAccount;
import com.taskflow.backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CurrentUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public CurrentUser loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userRepository.findByEmail(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toCurrentUser(user);
    }

    public CurrentUser loadCurrentUser(UserAccount user) {
        return toCurrentUser(user);
    }

    private CurrentUser toCurrentUser(UserAccount user) {
        return new CurrentUser(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPasswordHash()
        );
    }
}
