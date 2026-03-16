package com.kits.jklub.repository;

import com.kits.jklub.model.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {

    // Custom query to find all questions marked as "daily"
    List<Question> findByIsDaily(boolean isDaily);

    // Custom query to find questions by category
    List<Question> findByCategory(String category);
}