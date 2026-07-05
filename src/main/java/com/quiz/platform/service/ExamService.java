package com.quiz.platform.service;

import com.quiz.platform.dto.ExamResultVO;
import com.quiz.platform.dto.StartExamResponse;
import com.quiz.platform.dto.SubmitExamRequest;

public interface ExamService {

    /**
     * 开始一场考试/练习：
     * - RANDOM策略：按试卷配置的题型数量，从题库已审核通过的题目里随机抽题，
     *   抽题结果写入 exam_record.question_snapshot
     * - FIXED策略：直接用 exam_paper_question 里预先配置好的题目
     */
    StartExamResponse startExam(Long paperId, Long userId);

    /**
     * 交卷：客观题（单选/多选/判断）自动判分；简答题暂不自动判分，
     * isCorrect置null等待人工阅卷。答错的客观题自动写入错题本。
     */
    ExamResultVO submitExam(SubmitExamRequest request, Long userId);
}
