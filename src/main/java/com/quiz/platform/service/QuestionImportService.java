package com.quiz.platform.service;

import org.springframework.web.multipart.MultipartFile;

public interface QuestionImportService {

    /**
     * 上传并解析题库文件的完整流程：
     * 1. 原始文件存入MinIO
     * 2. 创建 import_task 记录
     * 3. 调用解析引擎
     * 4. 解析出的题目写入 question 表（review_status=PENDING）
     * 5. 无法归类的段落写入 import_anomaly 表
     * 6. 回写 import_task 统计结果
     */
    Long importDocx(MultipartFile file, Long bankId, String parseMode, Long operatorId);
}
