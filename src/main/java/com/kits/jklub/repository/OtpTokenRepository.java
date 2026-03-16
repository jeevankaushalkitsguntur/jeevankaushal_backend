package com.kits.jklub.repository;

import com.kits.jklub.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {

    /**
     * Finds an OTP token by the user's unique identifier (email/phone number).
     */
    Optional<OtpToken> findByIdentifier(String identifier);

}