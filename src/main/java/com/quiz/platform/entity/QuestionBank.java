package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subject;
    private Long deptId;
    /** MANUAL / IMPORT */
    private String sourceType;
    private Integer singleCount;
    private Integer multipleCount;
    private Integer judgeCount;
    private Integer shortCount;
    private Long creatorId;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleteFlag;
}
