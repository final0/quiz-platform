package com.quiz.platform.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitExamRequest {
    private Long recordId;
    private List<AnswerItem> answers;
}
