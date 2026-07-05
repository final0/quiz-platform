package com.quiz.platform.dto;

import lombok.Data;

@Data
public class AnswerItem {
    private Long questionId;
    /** 单选传"C"；多选传"ABD"；判断传"对"/"错"；简答传文本 */
    private String answer;
}
