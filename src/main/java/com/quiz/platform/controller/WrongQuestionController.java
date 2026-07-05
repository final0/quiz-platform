package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.entity.Question;
import com.quiz.platform.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wrong-book")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping
    public Result<List<Question>> list(@RequestParam Long userId,
                                        @RequestParam(defaultValue = "true") boolean onlyUnmastered) {
        return Result.ok(wrongQuestionService.listWrongQuestions(userId, onlyUnmastered));
    }

    @PostMapping("/mastered")
    public Result<Void> markMastered(@RequestParam Long userId, @RequestParam Long questionId) {
        wrongQuestionService.markMastered(userId, questionId);
        return Result.ok();
    }
}
