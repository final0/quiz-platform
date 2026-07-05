package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_paper")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long bankId;
    private Long deptId;
    /** EXAM / PRACTICE */
    private String mode;
    /** FIXED / RANDOM */
    private String composeStrategy;

    private Integer singleCount;
    private BigDecimal singleScore;
    private Integer multipleCount;
    private BigDecimal multipleScore;
    private Integer judgeCount;
    private BigDecimal judgeScore;

    private Integer durationMinutes;
    private BigDecimal passScore;
    private BigDecimal totalScore;

    private Integer status;
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
