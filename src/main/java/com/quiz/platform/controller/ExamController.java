package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.dto.ExamResultVO;
import com.quiz.platform.dto.StartExamResponse;
import com.quiz.platform.dto.SubmitExamRequest;
import com.quiz.platform.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    // TODO: userId同样应来自登录态
    @PostMapping("/start")
    public Result<StartExamResponse> start(@RequestParam Long paperId, @RequestParam Long userId) {
        return Result.ok(examService.startExam(paperId, userId));
    }

    @PostMapping("/submit")
    public Result<ExamResultVO> submit(@RequestBody SubmitExamRequest request, @RequestParam Long userId) {
        return Result.ok(examService.submitExam(request, userId));
    }
}
