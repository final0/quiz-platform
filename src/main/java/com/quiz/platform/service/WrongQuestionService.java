package com.quiz.platform.service;

import com.quiz.platform.entity.Question;
import java.util.List;

public interface WrongQuestionService {

    void recordWrong(Long userId, Long questionId);

    List<Question> listWrongQuestions(Long userId, boolean onlyUnmastered);

    void markMastered(Long userId, Long questionId);
}
