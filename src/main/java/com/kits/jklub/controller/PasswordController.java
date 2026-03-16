package com.kits.jklub.controller;

import com.kits.jklub.model.OtpToken;
import com.kits.jklub.model.User;
import com.kits.jklub.repository.OtpTokenRepository;
import com.kits.jklub.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class PasswordController {

    private final OtpTokenRepository otpTokenRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(OtpTokenRepository otpTokenRepository,
                              UserService userService,
                              PasswordEncoder passwordEncoder) {

        this.otpTokenRepository = otpTokenRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    /**
     * Reset Password Endpoint
     * URL: POST /api/v1/password/reset
     *
     * Body:
     * {
     *   "rollNo": "23JR1A4265",
     *   "otp": "123456",
     *   "newPassword": "newpass123"
     * }
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> req){

        try {

            String rollNo = req.get("rollNo").toUpperCase();
            String otp = req.get("otp");
            String newPassword = req.get("newPassword");

            Optional<OtpToken> tokenOpt = otpTokenRepository.findByIdentifier(rollNo);

            if(tokenOpt.isEmpty()){
                return ResponseEntity.badRequest()
                        .body(Map.of("message","OTP not found"));
            }

            OtpToken token = tokenOpt.get();

            if(!token.getOtp().equals(otp)){
                return ResponseEntity.badRequest()
                        .body(Map.of("message","Invalid OTP"));
            }

            if(token.getExpiryDate().isBefore(LocalDateTime.now())){
                return ResponseEntity.badRequest()
                        .body(Map.of("message","OTP expired"));
            }

            User user = userService.findByRollNo(rollNo);

            if(user == null){
                return ResponseEntity.badRequest()
                        .body(Map.of("message","User not found"));
            }

            user.setHashedPassword(passwordEncoder.encode(newPassword));

            userService.saveUserDirectly(user);

            otpTokenRepository.delete(token);

            return ResponseEntity.ok(
                    Map.of("message","Password reset successful")
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message","Password reset failed"));

        }

    }

}