package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.entity.ImportAnomaly;
import com.quiz.platform.entity.ImportTask;
import com.quiz.platform.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final QuestionImportService questionImportService;

    @PostMapping("/docx")
    public Result<Long> importDocx(@RequestParam MultipartFile file,
                                    @RequestParam Long bankId,
                                    @RequestParam(defaultValue = "RULE") String parseMode,
                                    @RequestParam Long operatorId) {
        Long taskId = questionImportService.importDocx(file, bankId, parseMode, operatorId);
        return Result.ok(taskId);
    }

    /** 查询导入任务详情（含解析统计），供上传完成后前端展示结果 */
    @GetMapping("/task/{id}")
    public Result<ImportTask> getTask(@PathVariable Long id) {
        return Result.ok(questionImportService.getTask(id));
    }

    /** 查询某次导入任务里无法归类的原始段落 */
    @GetMapping("/anomalies")
    public Result<List<ImportAnomaly>> listAnomalies(@RequestParam Long taskId) {
        return Result.ok(questionImportService.listAnomalies(taskId));
    }
}
