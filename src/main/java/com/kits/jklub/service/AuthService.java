package com.kits.jklub.service;

import com.kits.jklub.model.OtpToken;
import com.kits.jklub.model.User;
import com.kits.jklub.repository.OtpTokenRepository;
import com.kits.jklub.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       OtpTokenRepository otpTokenRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * REGISTRATION STEP 1: Send OTP to verify identity.
     * Checks if the Roll Number is already registered.
     */
    public void requestRegistrationOtp(String rollNo, String email) {
        String normalizedRollNo = rollNo.toUpperCase(); // Ensure uppercase normalization

        if (userRepository.findByRollNo(normalizedRollNo).isPresent()) {
            throw new RuntimeException("Roll Number " + normalizedRollNo + " is already registered.");
        }

        generateAndSend6DigitOtp(normalizedRollNo, email, "Your JKlub Registration Code");
    }

    /**
     * REGISTRATION STEP 2: Finalize Account Creation.
     * Verifies the OTP before saving the User to the database.
     */
    public User verifyAndRegister(User user, String otp) {
        String rollNo = user.getRollNo().toUpperCase();

        if (!validateOtp(rollNo, otp)) {
            throw new RuntimeException("Invalid or expired verification code.");
        }

        // Finalize User Object
        user.setRollNo(rollNo);
        user.setLoginIdentifier(rollNo);
        user.setHashedPassword(passwordEncoder.encode(user.getHashedPassword()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setCourseCompletionPercentage(0.0);
        user.setSubscribed(false);
        user.setEmailVerified(true);

        User savedUser = userRepository.save(user);
        otpTokenRepository.findByIdentifier(rollNo).ifPresent(otpTokenRepository::delete); // Clean up
        return savedUser;
    }

    /**
     * PASSWORD RESET STEP 1: Send OTP to the email associated with the Roll No.
     * Automatically retrieves the email from the user's profile.
     */
    // AuthService.java

    // AuthService.java
    public void requestPasswordResetOtp(String rollNo) {
        String normalizedRollNo = rollNo.toUpperCase().trim();

        // Check loginIdentifier FIRST as it's your primary auth key
        User user = userRepository.findByLoginIdentifier(normalizedRollNo)
                .orElseGet(() -> userRepository.findByRollNo(normalizedRollNo)
                        .orElseThrow(() -> new RuntimeException("User not found with Roll No: " + normalizedRollNo)));

        generateAndSend6DigitOtp(normalizedRollNo, user.getEmail(), "Your JKlub Password Reset Code");
    }

    /**
     * PASSWORD RESET STEP 2: Verify and Update Password.
     * Updates the hashed password only if OTP is valid.
     */
    public void verifyAndResetPassword(String rollNo, String otp, String newPassword) {
        String normalizedRollNo = rollNo.toUpperCase();

        if (!validateOtp(normalizedRollNo, otp)) {
            throw new RuntimeException("Invalid or expired verification code.");
        }

        User user = userRepository.findByRollNo(normalizedRollNo)
                .orElseThrow(() -> new RuntimeException("User context lost during reset."));

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpTokenRepository.findByIdentifier(normalizedRollNo).ifPresent(otpTokenRepository::delete);
    }

    // --- Private Utility Methods ---

    private void generateAndSend6DigitOtp(String identifier, String email, String subject) {
        String otp = String.format("%06d", new Random().nextInt(999999)); // Generate 6-digit code

        OtpToken token = otpTokenRepository.findByIdentifier(identifier)
                .orElse(new OtpToken());
        token.setIdentifier(identifier);
        token.setOtp(otp);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // 10-minute validity

        otpTokenRepository.save(token);
        emailService.sendOtpEmail(email, otp); //
    }

    private boolean validateOtp(String identifier, String otp) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByIdentifier(identifier);
        return tokenOpt.isPresent() &&
                tokenOpt.get().getOtp().equals(otp) &&
                tokenOpt.get().getExpiryDate().isAfter(LocalDateTime.now());
    }
}