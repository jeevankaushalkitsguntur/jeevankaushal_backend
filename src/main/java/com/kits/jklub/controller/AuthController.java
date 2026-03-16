package com.kits.jklub.controller;

import com.kits.jklub.dto.AuthenticationResponse;
import com.kits.jklub.dto.UserResponseDTO;
import com.kits.jklub.model.User;
import com.kits.jklub.service.UserService;
import com.kits.jklub.util.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtils jwtUtils,
                          UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Handles user registration.
     * Maps to the permitted /api/v1/register endpoint in SecurityConfig.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(UserResponseDTO.fromUser(registeredUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Handles user login and returns a JWT.
     * Maps to the permitted /api/v1/login endpoint in SecurityConfig.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String loginIdentifier = request.get("loginIdentifier").toUpperCase();
        String password = request.get("password");

        // 1. Authenticate the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginIdentifier, password)
        );

        // 2. Load user details and generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginIdentifier);
        final String jwt = jwtUtils.generateToken(userDetails);

        // 3. Fetch user data to return in response
        User user = userService.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found post-auth"));

        return ResponseEntity.ok(new AuthenticationResponse(jwt, UserResponseDTO.fromUser(user)));
    }
}