package com.quiz.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quiz.platform.entity.Question;
import com.quiz.platform.entity.WrongQuestionBook;
import com.quiz.platform.mapper.QuestionMapper;
import com.quiz.platform.mapper.WrongQuestionBookMapper;
import com.quiz.platform.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionBookMapper wrongQuestionBookMapper;
    private final QuestionMapper questionMapper;

    @Override
    public void recordWrong(Long userId, Long questionId) {
        WrongQuestionBook existing = wrongQuestionBookMapper.selectOne(
                Wrappers.<WrongQuestionBook>lambdaQuery()
                        .eq(WrongQuestionBook::getUserId, userId)
                        .eq(WrongQuestionBook::getQuestionId, questionId));

        if (existing == null) {
            WrongQuestionBook wq = new WrongQuestionBook();
            wq.setUserId(userId);
            wq.setQuestionId(questionId);
            wq.setWrongCount(1);
            wq.setLastWrongTime(LocalDateTime.now());
            wq.setMasteredFlag(0);
            wrongQuestionBookMapper.insert(wq);
        } else {
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongTime(LocalDateTime.now());
            existing.setMasteredFlag(0);
            wrongQuestionBookMapper.updateById(existing);
        }
    }

    @Override
    public List<Question> listWrongQuestions(Long userId, boolean onlyUnmastered) {
        LambdaQueryWrapper<WrongQuestionBook> qw = Wrappers.<WrongQuestionBook>lambdaQuery()
                .eq(WrongQuestionBook::getUserId, userId);
        if (onlyUnmastered) {
            qw.eq(WrongQuestionBook::getMasteredFlag, 0);
        }
        List<WrongQuestionBook> records = wrongQuestionBookMapper.selectList(qw);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> questionIds = records.stream().map(WrongQuestionBook::getQuestionId).toList();
        return questionMapper.selectBatchIds(questionIds);
    }

    @Override
    public void markMastered(Long userId, Long questionId) {
        WrongQuestionBook wq = wrongQuestionBookMapper.selectOne(
                Wrappers.<WrongQuestionBook>lambdaQuery()
                        .eq(WrongQuestionBook::getUserId, userId)
                        .eq(WrongQuestionBook::getQuestionId, questionId));
        if (wq != null) {
            wq.setMasteredFlag(1);
            wrongQuestionBookMapper.updateById(wq);
        }
    }
}
