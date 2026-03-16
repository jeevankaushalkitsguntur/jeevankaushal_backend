package com.kits.jklub.service;
import java.util.List;
import com.kits.jklub.model.User;
import com.kits.jklub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    public UserRepository userRepository; // Changed from private to public to fix DataController access error

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Resolves the "userRepository has private access" error in DataController.
     * Provides public access to the repository instance via a method.
     */
    public UserRepository getUserRepository() {
        return userRepository;
    }

    /**
     * Resolves the "'void' type not allowed" error in DataController.
     * Updates a user's streak and returns the updated User object so it can be
     * used in API responses.
     */
    public User updateStreak(String rollNo) {
        User user = findByRollNo(rollNo);
        if (user != null) {
            updateStreakInternal(user);
            return userRepository.save(user);
        }
        return null;
    }

    /**
     * Resolves the "cannot find symbol" error in DataController.
     * Updates a user's subscription status and returns the updated user.
     */
    public User subscribeUser(String rollNo) {
        User user = findByRollNo(rollNo);
        if (user != null) {
            user.setSubscribed(true);
            // Default subscription for 1 year
            user.setSubscriptionExpiryDate(LocalDateTime.now().plusYears(1));
            return userRepository.save(user);
        }
        return null;
    }

    /**
     * Finds a user by their Roll Number (Case Insensitive)
     */
    public User findByRollNo(String rollNo) {
        return userRepository.findByRollNo(rollNo.toUpperCase()).orElse(null);
    }

    /**
     * Finds a user by their login identifier.
     */
    public Optional<User> findByLoginIdentifier(String loginIdentifier) {
        return userRepository.findByLoginIdentifier(loginIdentifier);
    }

    /**
     * Direct save method for simple updates.
     */
    public void saveUserDirectly(User user) {
        userRepository.save(user);
    }

    /**
     * Registration logic: Sanitizes roll number and hashes password.
     */
    public User registerUser(User user) {
        String normalizedRollNo = user.getRollNo().toUpperCase();

        if (userRepository.findByRollNo(normalizedRollNo).isPresent()) {
            throw new RuntimeException("User with Roll Number " + normalizedRollNo + " already exists.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered with another account.");
        }
        user.setRollNo(user.getRollNo().toUpperCase());
        user.setLoginIdentifier(user.getRollNo());
        user.setHashedPassword(passwordEncoder.encode(user.getHashedPassword()));
        user.setEmailVerified(true);
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setCourseCompletionPercentage(0.0);
        user.setCurrentStreak(0);
        user.setSubscribed(false);
        return userRepository.save(user);
    }

    /**
     * Logic to update course progress with a 10% paywall for free users.
     */
    // UserService.java
    public User updateCourseProgress(String loginIdentifier, double percentage) {
        User user = userRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Existing Paywall Logic
        if (!user.isSubscribed() && percentage > 10.0) {
            percentage = 10.0;
        }

        // New Fix: If percentage hits 100, flip the completion flag
        if (percentage >= 100.0) {
            user.setCourseCompleted(true); // This unlocks the certificate
        }

        user.setCourseCompletionPercentage(percentage);
        return userRepository.save(user);
    }
    /**
     * New Method: Update course progress when a module is completed.
     * This does NOT change existing progress logic.
     */
    public User completeModule(String loginIdentifier, String moduleName, int totalModules) {

        User user = userRepository.findByLoginIdentifier(loginIdentifier)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> completedModules = user.getCompletedModules();

        // Prevent duplicate module completion
        if (!completedModules.contains(moduleName)) {
            completedModules.add(moduleName);
        }

        user.setCompletedModules(completedModules);
        user.setLastCompletedModule(moduleName);
        user.setTotalModulesCompleted(completedModules.size());

        // Calculate percentage automatically
        double percentage = ((double) completedModules.size() / totalModules) * 100;

        // Apply existing paywall logic
        if (!user.isSubscribed() && percentage > 10.0) {
            percentage = 10.0;
        }

        user.setCourseCompletionPercentage(percentage);

        // Mark course completed
        if (percentage >= 100.0) {
            user.setCourseCompleted(true);
        }

        return userRepository.save(user);
    }

    /**
     * Logic for OTP verification and password reset.
     */
    public boolean verifyAndResetPassword(String rollNo, String otp, String newPassword) {
        User user = findByRollNo(rollNo);

        if (user != null &&
                user.getOtp() != null &&
                user.getOtp().equals(otp) &&
                user.getOtpExpiry() != null &&
                LocalDateTime.now().isBefore(user.getOtpExpiry())) {

            user.setHashedPassword(passwordEncoder.encode(newPassword));
            user.setOtp(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Internal logic to handle daily streaks.
     */
    private void updateStreakInternal(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastPlayed = user.getLastPlayedDate();

        if (lastPlayed == null) {
            user.setCurrentStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastPlayed, today);

            if (daysBetween == 1) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (daysBetween > 1) {
                user.setCurrentStreak(1);
            }
        }


        // Track the longest streak ever achieved
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        user.setLastPlayedDate(today);
    }
}