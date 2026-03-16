package com.kits.jklub.config;

import com.kits.jklub.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

/**
 * Configuration class for core Spring Security components not handled in SecurityConfig.
 */
@Configuration
public class ApplicationConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;

    public ApplicationConfig(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder, AuthenticationProvider authenticationProvider) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Expose the AuthenticationManager bean, which is required for the /login endpoint.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return new ProviderManager(Collections.singletonList(authenticationProvider));
    }
}