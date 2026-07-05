package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "exam_record", autoResultMap = true)
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long userId;
    /** EXAM / PRACTICE，从 exam_paper 冗余一份 */
    private String mode;

    /** 本次实际抽到的题目快照 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<QuestionSnapshotItem> questionSnapshot;

    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    /** IN_PROGRESS / SUBMITTED / TIMEOUT */
    private String status;
    private BigDecimal score;
    private Integer passFlag;

    @Data
    public static class QuestionSnapshotItem {
        private Long questionId;
        private BigDecimal score;
    }
}
