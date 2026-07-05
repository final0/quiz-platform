package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}
