package com.kits.jklub.controller;

import com.kits.jklub.model.OtpToken;
import com.kits.jklub.model.User;
import com.kits.jklub.repository.OtpTokenRepository;
import com.kits.jklub.service.EmailService;
import com.kits.jklub.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/otp")
public class OtpController {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public OtpController(OtpTokenRepository otpTokenRepository,
                         EmailService emailService,
                         UserService userService,
                         PasswordEncoder passwordEncoder) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {

        String loginIdentifier = request.get("loginIdentifier");
        String email = request.get("email");

        // Prevent null or empty roll number
        if (loginIdentifier == null || loginIdentifier.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Roll Number is required"));
        }

        loginIdentifier = loginIdentifier.toUpperCase();

        // If email not provided (forgot password), fetch from DB
        if (email == null || email.isEmpty()) {

            User user = userService.findByRollNo(loginIdentifier);

            if (user == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "User with this Roll Number does not exist."));
            }

            email = user.getEmail();
        }

        // Generate 6-digit OTP
        // Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

// Remove previous OTP if exists

        otpTokenRepository.findByIdentifier(loginIdentifier)
                .ifPresent(otpTokenRepository::delete);
     

// Create new OTP record
        OtpToken otpToken = new OtpToken();
        otpToken.setIdentifier(loginIdentifier);
        otpToken.setOtp(otp);
        otpToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));

// Save to MongoDB
        otpTokenRepository.save(otpToken);

        System.out.println("OTP SAVED TO DB FOR: " + loginIdentifier);

        System.out.println("Sending OTP to: " + email);
        System.out.println("OTP Generated: " + otp);

        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok(
                Map.of("message", "OTP sent successfully to " + email)
        );
    }

    @PostMapping("/verify-only")
    public ResponseEntity<?> verifyOnly(@RequestBody Map<String, String> request) {

        String loginIdentifier = request.get("loginIdentifier");
        String receivedOtp = request.get("otp");

        if (loginIdentifier == null || loginIdentifier.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Roll Number is required"));
        }

        loginIdentifier = loginIdentifier.toUpperCase();

        Optional<OtpToken> otpOpt =
                otpTokenRepository.findByIdentifier(loginIdentifier);

        if (otpOpt.isPresent() && otpOpt.get().getOtp().equals(receivedOtp)) {

            if (otpOpt.get().getExpiryDate().isBefore(LocalDateTime.now())) {

                return ResponseEntity.badRequest()
                        .body(Map.of("message", "OTP has expired."));
            }

            return ResponseEntity.ok(
                    Map.of("message", "OTP verified successfully!")
            );
        }

        return ResponseEntity.badRequest()
                .body(Map.of("message", "Invalid OTP."));
    }
}