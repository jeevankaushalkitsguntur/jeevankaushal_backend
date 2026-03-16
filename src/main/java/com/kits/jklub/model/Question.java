package com.kits.jklub.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "quiz_questions")
public class Question {
    @Id
    private String id;

    private String category;     // e.g., "Grammar", "Tenses", "Vocabulary"
    private String questionText; // The actual question
    private List<String> options; // List of 4 multiple choice options
    private int correctOptionIndex; // Index of the correct answer (0 to 3)

    private boolean isDaily;     // Flag to mark if this is a daily challenge question
}