package com.kits.jklub.controller;

import com.kits.jklub.model.User;
import com.kits.jklub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private UserRepository userRepository;

    private static final double TOTAL_MODULES = 60.0;

    @PostMapping("/complete")
    public double completeModule(
            @RequestParam String moduleId,
            Authentication authentication) {

        String loginIdentifier = authentication.getName();

        User user = userRepository
                .findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // initialize list
        if (user.getCompletedModules() == null) {
            user.setCompletedModules(new ArrayList<>());
        }

        List<String> modules = user.getCompletedModules();

        // prevent duplicates
        if (!modules.contains(moduleId)) {
            modules.add(moduleId);
        }

        user.setCompletedModules(modules);

        double progress =  ((modules.size() * 100.0) / TOTAL_MODULES);

        user.setCourseCompletionPercentage(progress);

        userRepository.save(user);

        return progress;
    }

    @GetMapping("/me")
    public int getMyProgress(Authentication authentication) {

        String userEmail = authentication.getName();

        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return (int) user.getCourseCompletionPercentage();
    }
}