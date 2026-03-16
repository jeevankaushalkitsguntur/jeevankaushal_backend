package com.kits.jklub.controller;

import com.kits.jklub.model.OtpToken;
import com.kits.jklub.model.User;
import com.kits.jklub.repository.OtpTokenRepository;
import com.kits.jklub.service.AuthService;
import com.kits.jklub.service.EmailService;
import com.kits.jklub.service.UserService;
import com.kits.jklub.dto.UserResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Unified Controller for all Authentication-related gates.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class UnifiedAuthController {

    private final AuthService authService;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final UserService userService;

    public UnifiedAuthController(AuthService authService,
                                 OtpTokenRepository otpTokenRepository,
                                 EmailService emailService,
                                 UserService userService) {

        this.authService = authService;
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
        this.userService = userService;
    }

    /* ---------------- REGISTRATION FLOW ---------------- */

    @PostMapping("/register/send-otp")
    public ResponseEntity<?> sendRegistrationOtp(@RequestBody Map<String, String> request) {
        try {

            String rollNo = request.get("rollNo").toUpperCase();
            String email = request.get("email");

            String otp = String.valueOf(100000 + new Random().nextInt(900000));

            OtpToken otpToken = otpTokenRepository
                    .findByIdentifier(rollNo)
                    .orElse(new OtpToken());

            otpToken.setIdentifier(rollNo);
            otpToken.setOtp(otp);
            otpToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));

            otpTokenRepository.save(otpToken);

            emailService.sendOtpEmail(email, otp);

            return ResponseEntity.ok(Map.of(
                    "message", "6-digit code sent to " + email
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> verifyAndRegister(@RequestBody Map<String, Object> request) {

        try {

            String otp = (String) request.get("otp");

            User user = new User();
            user.setName((String) request.get("name"));
            user.setRollNo((String) request.get("rollNo"));
            user.setEmail((String) request.get("email"));
            user.setHashedPassword((String) request.get("password"));
            user.setDepartment((String) request.get("department"));

            User registeredUser = authService.verifyAndRegister(user, otp);

            return ResponseEntity.ok(
                    UserResponseDTO.fromUser(registeredUser)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /* ---------------- PASSWORD RESET FLOW ---------------- */

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {

        try {

            String rollNo = request.get("rollNo").toUpperCase();

            User user = userService.findByRollNo(rollNo);

            if (user == null) {

                return ResponseEntity.badRequest()
                        .body(Map.of("error", "User not found"));
            }

            String email = user.getEmail();

            String otp = String.valueOf(100000 + new Random().nextInt(900000));

            OtpToken otpToken = otpTokenRepository
                    .findByIdentifier(rollNo)
                    .orElse(new OtpToken());

            otpToken.setIdentifier(rollNo);
            otpToken.setOtp(otp);
            otpToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));

            otpTokenRepository.save(otpToken);

            emailService.sendOtpEmail(email, otp);

            return ResponseEntity.ok(Map.of(
                    "message", "Reset code sent to your registered email address."
            ));

        } catch (Exception e) {

            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        try {

            String rollNo = request.get("rollNo");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");

            authService.verifyAndResetPassword(rollNo, otp, newPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successful! Please login."
            ));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}