package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("import_anomaly")
public class ImportAnomaly {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private String rawText;
    private Integer resolved;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
