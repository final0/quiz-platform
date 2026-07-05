package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "question", autoResultMap = true)
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    /** SINGLE / MULTIPLE / JUDGE / SHORT_ANSWER */
    private String type;
    private String stem;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> options;

    /** 单选"C" / 多选"ABD" / 判断"对"/"错" / 简答为null */
    private String answer;
    private String analysis;
    private Integer difficulty;
    private String tags;

    private String sourceNum;
    /** HIGH / LOW */
    private String parseConfidence;
    /** PENDING / APPROVED / REJECTED */
    private String reviewStatus;

    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleteFlag;
}
