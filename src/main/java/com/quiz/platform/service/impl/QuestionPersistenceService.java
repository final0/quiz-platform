package com.quiz.platform.service.impl;

import com.quiz.platform.common.enums.ParseConfidence;
import com.quiz.platform.common.enums.ReviewStatus;
import com.quiz.platform.entity.ImportAnomaly;
import com.quiz.platform.entity.Question;
import com.quiz.platform.mapper.ImportAnomalyMapper;
import com.quiz.platform.mapper.QuestionMapper;
import com.quiz.platform.parse.ParseResult;
import com.quiz.platform.parse.ParsedQuestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 落库逻辑单独抽成一个bean，而不是QuestionImportServiceImpl里一个@Transactional方法。
 *
 * 原因：Spring的@Transactional基于AOP代理实现，同一个类里方法内部互相调用（this.xxx()）
 * 不会经过代理，注解会静默失效——这跟你之前在 StatDirtyAsyncService 上踩过的
 * @Async自调用坑是完全同一类问题，这里提前避开。
 */
@Component
@RequiredArgsConstructor
public class QuestionPersistenceService {

    private final QuestionMapper questionMapper;
    private final ImportAnomalyMapper importAnomalyMapper;

    @Transactional(rollbackFor = Exception.class)
    public void persistQuestions(Long bankId, Long operatorId, ParseResult result, Long taskId) {
        for (ParsedQuestion pq : result.getQuestions()) {
            Question q = new Question();
            q.setBankId(bankId);
            q.setType(pq.getType().name());
            q.setStem(pq.getStem());
            q.setOptions(pq.getOptions().isEmpty() ? null : pq.getOptions());
            q.setAnswer(pq.getAnswer());
            q.setDifficulty(2);
            q.setSourceNum(pq.getSourceNum());
            q.setParseConfidence(pq.getConfidence() == ParsedQuestion.Confidence.HIGH
                    ? ParseConfidence.HIGH.name() : ParseConfidence.LOW.name());
            q.setReviewStatus(ReviewStatus.PENDING.name());
            q.setCreatorId(operatorId);
            questionMapper.insert(q);
        }

        for (String anomaly : result.getAnomalies()) {
            ImportAnomaly a = new ImportAnomaly();
            a.setTaskId(taskId);
            a.setRawText(anomaly);
            a.setResolved(0);
            importAnomalyMapper.insert(a);
        }
    }
}
