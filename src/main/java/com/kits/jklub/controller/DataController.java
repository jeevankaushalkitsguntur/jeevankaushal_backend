package com.kits.jklub.controller;

import com.kits.jklub.model.User;
import com.kits.jklub.dto.UserResponseDTO;
import com.kits.jklub.service.UserService;
import com.kits.jklub.service.RazorpayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.kits.jklub.service.CertificateService;

@RestController
@RequestMapping("/api/v1")
public class DataController {

    private final UserService userService;
    private final RazorpayService razorpayService;
    private final CertificateService certificateService;

    public DataController(UserService userService, RazorpayService razorpayService,CertificateService certificateService) {
        this.userService = userService;
        this.razorpayService = razorpayService;
        this.certificateService = certificateService;

    }
    @GetMapping("/course/download-certificate")
    public ResponseEntity<byte[]> downloadCertificate(@RequestParam String loginIdentifier) {
        try {
            // 1. Fetch user and verify eligibility
            User user = userService.findByLoginIdentifier(loginIdentifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getCourseCompletionPercentage() < 100) {
                return ResponseEntity.badRequest().body(null); // Or a custom error message
            }

            // 2. Generate PDF using the service
            byte[] contents = certificateService.generateCertificate(user.getName());

            // 3. Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "JKlub_Certificate_" + user.getRollNo() + ".pdf";
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/course/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> request) {
        try {
            String loginIdentifier = request.get("loginIdentifier");
            String orderJson = razorpayService.createSubscriptionOrder(loginIdentifier);
            return ResponseEntity.ok(orderJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/course/update-progress")
    public ResponseEntity<?> updateProgress(@RequestBody Map<String, Object> request) {
        String rollNo = (String) request.get("loginIdentifier");
        double percentage = Double.parseDouble(request.get("percentage").toString());
        return ResponseEntity.ok(UserResponseDTO.fromUser(userService.updateCourseProgress(rollNo, percentage)));
    }

    @PostMapping("/course/update-streak")
    public ResponseEntity<?> updateStreak(@RequestBody Map<String, String> request) {
        String rollNo = request.get("loginIdentifier");
        return ResponseEntity.ok(UserResponseDTO.fromUser(userService.updateStreak(rollNo)));
    }

    @PostMapping("/course/submit-exam")
    public ResponseEntity<?> submitExam(@RequestBody Map<String, Object> request) {
        String rollNo = (String) request.get("loginIdentifier");
        int score = Integer.parseInt(request.get("score").toString());
        boolean passed = (boolean) request.get("passed");

        User user = userService.findByLoginIdentifier(rollNo).orElseThrow();
        user.setExamAttempted(true);
        user.setExamPassed(passed);
        user.setExamScore(score);

        if (passed) {
            user.setCourseCompletionPercentage(100.0);
            user.setCourseCompleted(true);
        }

        return ResponseEntity.ok(UserResponseDTO.fromUser(userService.userRepository.save(user)));
    }

    @PostMapping("/course/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) {
        String orderId = request.get("razorpay_order_id");
        String paymentId = request.get("razorpay_payment_id");
        String signature = request.get("razorpay_signature");
        String loginIdentifier = request.get("loginIdentifier");

        if (razorpayService.verifyPaymentSignature(orderId, paymentId, signature)) {
            User user = userService.subscribeUser(loginIdentifier);
            return ResponseEntity.ok(UserResponseDTO.fromUser(user));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Payment verification failed."));
    }

}