package com.kits.jklub.controller;

import com.kits.jklub.model.Question;
import com.kits.jklub.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quiz")
public class DailyQuizController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/daily")
    public ResponseEntity<Question> getDailyQuestion() {
        // Use MongoDB $sample to get 1 random question where isDaily is true
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(org.springframework.data.mongodb.core.query.Criteria.where("isDaily").is(true)),
                Aggregation.sample(1)
        );

        AggregationResults<Question> results = mongoTemplate.aggregate(aggregation, "quiz_questions", Question.class);
        Question randomQuestion = results.getUniqueMappedResult();

        if (randomQuestion == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(randomQuestion);
    }
}