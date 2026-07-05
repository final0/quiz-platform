package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.entity.QuestionBank;
import com.quiz.platform.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class QuestionBankController {

    private final QuestionBankService questionBankService;

    @PostMapping
    public Result<QuestionBank> create(@RequestBody QuestionBank bank) {
        questionBankService.save(bank);
        return Result.ok(bank);
    }

    @GetMapping
    public Result<List<QuestionBank>> list() {
        return Result.ok(questionBankService.list());
    }

    @GetMapping("/{id}")
    public Result<QuestionBank> get(@PathVariable Long id) {
        return Result.ok(questionBankService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionBankService.removeById(id);
        return Result.ok();
    }
}
