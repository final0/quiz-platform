package com.quiz.platform.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StartExamResponse {
    private Long recordId;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private List<QuestionDisplayVO> questions;
}
