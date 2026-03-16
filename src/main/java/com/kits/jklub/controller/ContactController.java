package com.kits.jklub.controller;

import com.kits.jklub.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ContactController {

    private final EmailService emailService;

    public ContactController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/contact")
    public ResponseEntity<?> sendContactMessage(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String message = request.get("message");

        if (name == null || email == null || message == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required."));
        }

        try {
            emailService.sendContactMessage(name, email, message);
            return ResponseEntity.ok(Map.of("message", "Message sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Failed to send message."));
        }
    }
}