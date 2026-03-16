package com.kits.jklub.dto;

import com.kits.jklub.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for public User responses.
 * Excludes sensitive data like hashedPassword and removed mobile fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private String id;
    private String name;
    private String loginIdentifier;
    private String role;
    private boolean isEmailVerified;
    // Removed: isPhoneVerified field
    private int currentStreak;
    private int longestStreak;

    // Course/Certificate Fields
    private double courseCompletionPercentage;
    private boolean isCourseCompleted;
    private String certificateStatus;
    private String certificateId;
    private boolean isCertified;


    // Subscription & Exam Fields
    private boolean isSubscribed;
    private LocalDateTime subscriptionExpiryDate;
    private boolean isExamAttempted;
    private boolean isExamPassed;
    private Integer examScore;
    private LocalDateTime examStartTime;


    /**
     * Factory method to create DTO from User model.
     * Synchronized with the Roll Number identifier and removed mobile logic.
     */
    public static UserResponseDTO fromUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getLoginIdentifier(),
                user.getRole(),
                user.isEmailVerified(),

                // Removed: user.isPhoneVerified() call
                user.getCurrentStreak(),
                user.getLongestStreak(),
                user.getCourseCompletionPercentage(),
                user.isCourseCompleted(),
                user.getCertificateStatus(),
                user.getCertificateId(),
                user.isCertified(),
                user.isSubscribed(),
                user.getSubscriptionExpiryDate(),
                user.isExamAttempted(),
                user.isExamPassed(),
                user.getExamScore(),
                user.getExamStartTime()
        );
    }
}