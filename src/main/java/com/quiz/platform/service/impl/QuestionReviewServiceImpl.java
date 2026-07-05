package com.quiz.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quiz.platform.common.BizException;
import com.quiz.platform.common.enums.ReviewStatus;
import com.quiz.platform.entity.Question;
import com.quiz.platform.mapper.QuestionMapper;
import com.quiz.platform.service.QuestionReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionReviewServiceImpl implements QuestionReviewService {

    private final QuestionMapper questionMapper;

    @Override
    public List<Question> listPending(Long bankId, boolean lowConfidenceOnly, int page, int size) {
        LambdaQueryWrapper<Question> qw = Wrappers.<Question>lambdaQuery()
                .eq(Question::getBankId, bankId)
                .eq(Question::getReviewStatus, ReviewStatus.PENDING.name())
                .orderByAsc(Question::getSourceNum);
        if (lowConfidenceOnly) {
            qw.eq(Question::getParseConfidence, "LOW");
        }
        Page<Question> result = questionMapper.selectPage(new Page<>(page, size), qw);
        return result.getRecords();
    }

    @Override
    public void approve(Long questionId) {
        Question q = mustExist(questionId);
        q.setReviewStatus(ReviewStatus.APPROVED.name());
        questionMapper.updateById(q);
    }

    @Override
    public void reject(Long questionId, String reason) {
        Question q = mustExist(questionId);
        q.setReviewStatus(ReviewStatus.REJECTED.name());
        q.setAnalysis((q.getAnalysis() == null ? "" : q.getAnalysis()) + "\n[驳回原因] " + reason);
        questionMapper.updateById(q);
    }

    @Override
    public void batchApprove(List<Long> questionIds) {
        for (Long id : questionIds) {
            approve(id);
        }
    }

    @Override
    public void updateAndApprove(Question question) {
        if (question.getId() == null) {
            throw new BizException("questionId不能为空");
        }
        question.setReviewStatus(ReviewStatus.APPROVED.name());
        questionMapper.updateById(question);
    }

    private Question mustExist(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BizException("题目不存在: " + id);
        }
        return q;
    }
}
