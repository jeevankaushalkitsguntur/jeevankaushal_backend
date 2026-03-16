package com.kits.jklub.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;
    private String rollNo;
    private String department;
    @Indexed(unique = true)
    private String email;
    private String loginIdentifier; // This is the field used for authentication
    private String hashedPassword;
    private String role; // "USER" or "ADMIN"

    private boolean isEmailVerified;
    private boolean isSubscribed;
    private boolean isCourseCompleted;

    private double courseCompletionPercentage;
    private int currentStreak;
    private int longestStreak;

    private LocalDate lastPlayedDate;
    private LocalDateTime createdAt;

    // Certificate Fields
    private String certificateStatus;
    private String certificateId;
    private boolean isCertified;

    // Subscription & Exam Fields
    private LocalDateTime subscriptionExpiryDate;
    private boolean isExamAttempted;
    private boolean isExamPassed;
    private Integer examScore;
    private LocalDateTime examStartTime;

    // Fields for Forgot Password OTP
    private String otp;
    private LocalDateTime otpExpiry;

    /* ---------------------------------------------------
       NEW FIELDS FOR COURSE PROGRESS TRACKING
       (Existing fields above remain unchanged)
       --------------------------------------------------- */

    // Stores completed module names


    private List<String> completedModules;



    // Stores last completed module (for resume learning)
    private String lastCompletedModule;

    // Total modules completed by the user
    private int totalModulesCompleted;

}