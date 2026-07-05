package com.quiz.platform.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/** 下发给考生作答页面的题目视图：不含答案和解析 */
@Data
public class QuestionDisplayVO {
    private Long questionId;
    private String type;
    private String stem;
    private Map<String, String> options;
    private BigDecimal score;
}
