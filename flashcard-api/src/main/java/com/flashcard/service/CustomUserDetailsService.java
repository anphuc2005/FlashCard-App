package com.flashcard.service;

import com.flashcard.exception.ResourceNotFoundException;
import com.flashcard.model.User;
import com.flashcard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads user-specific data for Spring Security authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail() != null && !user.getEmail().isEmpty() ? user.getEmail() : user.getId(), // Fallback to ID if email is missing in bad DB records
                user.getPassword() != null && !user.getPassword().isEmpty() ? user.getPassword() : "{noop}social_login", // Fix: prevent crash if social user has null or empty password
                user.isEnabled(),
                true, true, true,
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}
