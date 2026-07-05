package com.quiz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("import_task")
public class ImportTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bankId;
    private String fileName;
    private String minioPath;
    /** DOCX / PDF / TXT */
    private String fileType;
    /** RULE / AI */
    private String parseMode;
    /** PENDING / PROCESSING / SUCCESS / FAILED */
    private String status;
    private Integer totalCount;
    private Integer highConfCount;
    private Integer lowConfCount;
    private Integer anomalyCount;
    private String errorMessage;
    private Long creatorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
}
