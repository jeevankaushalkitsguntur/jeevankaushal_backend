package com.kits.jklub.repository;

import com.kits.jklub.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // This is the specific method required by CustomUserDetailsService
    Optional<User> findByLoginIdentifier(String loginIdentifier);


    // This is the method required by UserService
    Optional<User> findByRollNo(String rollNo);

    Optional<User> findByEmail(String email);
}
