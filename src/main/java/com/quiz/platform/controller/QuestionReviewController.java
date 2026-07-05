package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.entity.Question;
import com.quiz.platform.service.QuestionReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class QuestionReviewController {

    private final QuestionReviewService questionReviewService;

    @GetMapping("/pending")
    public Result<List<Question>> listPending(@RequestParam Long bankId,
                                               @RequestParam(defaultValue = "false") boolean lowConfidenceOnly,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.ok(questionReviewService.listPending(bankId, lowConfidenceOnly, page, size));
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        questionReviewService.approve(id);
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String reason) {
        questionReviewService.reject(id, reason);
        return Result.ok();
    }

    @PostMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody List<Long> questionIds) {
        questionReviewService.batchApprove(questionIds);
        return Result.ok();
    }

    @PutMapping("/update-and-approve")
    public Result<Void> updateAndApprove(@RequestBody Question question) {
        questionReviewService.updateAndApprove(question);
        return Result.ok();
    }
}
