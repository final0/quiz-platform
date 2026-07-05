package com.quiz.platform.service;

import com.quiz.platform.entity.ImportAnomaly;
import com.quiz.platform.entity.ImportTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface QuestionImportService {

    /**
     * 上传并解析题库文件的完整流程：
     * 1. 原始文件存入MinIO
     * 2. 创建 import_task 记录
     * 3. 调用解析引擎
     * 4. 解析出的题目写入 question 表
     * 5. 无法归类的段落写入 import_anomaly 表
     * 6. 回写 import_task 统计结果
     */
    Long importDocx(MultipartFile file, Long bankId, String parseMode, Long operatorId);

    /** 查询导入任务详情（含解析统计），供前端上传完成后展示结果 */
    ImportTask getTask(Long taskId);

    /** 查询某次导入任务中无法归类的原始段落，供人工在前端手动处理 */
    List<ImportAnomaly> listAnomalies(Long taskId);
}
