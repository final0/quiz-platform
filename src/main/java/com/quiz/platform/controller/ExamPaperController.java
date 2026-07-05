package com.quiz.platform.controller;

import com.quiz.platform.common.Result;
import com.quiz.platform.entity.ExamPaper;
import com.quiz.platform.service.ExamPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class ExamPaperController {

    private final ExamPaperService examPaperService;

    @PostMapping
    public Result<ExamPaper> create(@RequestBody ExamPaper paper) {
        paper.setSingleCount(defaultZero(paper.getSingleCount()));
        paper.setMultipleCount(defaultZero(paper.getMultipleCount()));
        paper.setJudgeCount(defaultZero(paper.getJudgeCount()));
        paper.setSingleScore(defaultZero(paper.getSingleScore()));
        paper.setMultipleScore(defaultZero(paper.getMultipleScore()));
        paper.setJudgeScore(defaultZero(paper.getJudgeScore()));

        var single = paper.getSingleScore().multiply(java.math.BigDecimal.valueOf(paper.getSingleCount()));
        var multiple = paper.getMultipleScore().multiply(java.math.BigDecimal.valueOf(paper.getMultipleCount()));
        var judge = paper.getJudgeScore().multiply(java.math.BigDecimal.valueOf(paper.getJudgeCount()));
        paper.setTotalScore(single.add(multiple).add(judge));
        examPaperService.save(paper);
        return Result.ok(paper);
    }

    private Integer defaultZero(Integer v) {
        return v == null ? 0 : v;
    }

    private java.math.BigDecimal defaultZero(java.math.BigDecimal v) {
        return v == null ? java.math.BigDecimal.ZERO : v;
    }

    @GetMapping
    public Result<List<ExamPaper>> list() {
        return Result.ok(examPaperService.list());
    }

    @GetMapping("/{id}")
    public Result<ExamPaper> get(@PathVariable Long id) {
        return Result.ok(examPaperService.getById(id));
    }
}
