package com.quiz.platform.service;

import com.quiz.platform.entity.Question;
import java.util.List;

/** 导入题目的人工审核：只有APPROVED的题目才能进入组卷候选池 */
public interface QuestionReviewService {

    List<Question> listPending(Long bankId, boolean lowConfidenceOnly, int page, int size);

    void approve(Long questionId);

    void reject(Long questionId, String reason);

    void batchApprove(List<Long> questionIds);

    void updateAndApprove(Question question);
}
