package com.kits.jklub.service;

import com.kits.jklub.model.User;
import com.kits.jklub.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service; // NEW IMPORT
import java.util.Collections;

@Service // ADDED: Registers this class as a globally available Spring Service bean
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Use constructor injection
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        // 1. Fetch your custom User document from MongoDB using the new field name
        User user = userRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login identifier: " + loginIdentifier));

        // 2. Map your custom User object to Spring Security's UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getLoginIdentifier(), // Use loginIdentifier as the Spring Security username
                user.getHashedPassword(), // The hashed password from MongoDB
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}