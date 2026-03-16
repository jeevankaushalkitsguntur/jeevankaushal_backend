package com.kits.jklub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * MongoDB Document for storing temporary OTPs used for
 * email/phone verification and password reset.
 */
@Document(collection = "otp_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {

    @Id
    private String id;

    // The user's email or phone number (unique identifier) or the UUID resetToken
    @Indexed(unique = true)
    private String identifier;

    // The generated 6-digit OTP (or "RESET" placeholder for the reset token)
    private String otp;

    // Time after which the document is invalid (TTL Index automatically handles deletion)
    @Indexed(expireAfterSeconds = 120)
    private LocalDateTime expiryDate;

}