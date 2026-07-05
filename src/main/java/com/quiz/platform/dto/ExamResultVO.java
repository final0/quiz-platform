package com.quiz.platform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ExamResultVO {
    private Long recordId;
    private BigDecimal score;
    private BigDecimal totalScore;
    private boolean pass;
    private List<AnswerResultItem> details;

    @Data
    public static class AnswerResultItem {
        private Long questionId;
        private String stem;
        private String userAnswer;
        private String correctAnswer;
        private Boolean correct;
        private BigDecimal score;
        private String analysis;
    }
}
