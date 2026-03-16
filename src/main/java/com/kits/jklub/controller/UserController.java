package com.kits.jklub.controller;

import com.kits.jklub.model.User;
import com.kits.jklub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication authentication) {

        String loginIdentifier = authentication.getName();

        return userService.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}