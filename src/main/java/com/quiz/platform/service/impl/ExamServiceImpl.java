package com.quiz.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quiz.platform.common.BizException;
import com.quiz.platform.common.enums.ComposeStrategy;
import com.quiz.platform.common.enums.RecordStatus;
import com.quiz.platform.common.enums.ReviewStatus;
import com.quiz.platform.dto.*;
import com.quiz.platform.entity.*;
import com.quiz.platform.mapper.*;
import com.quiz.platform.service.ExamService;
import com.quiz.platform.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerDetailMapper examAnswerDetailMapper;
    private final QuestionMapper questionMapper;
    private final WrongQuestionService wrongQuestionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StartExamResponse startExam(Long paperId, Long userId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || paper.getStatus() == 0) {
            throw new BizException("试卷不存在或已停用");
        }

        List<ExamRecord.QuestionSnapshotItem> snapshot;
        List<Question> questions;

        if (ComposeStrategy.FIXED.name().equals(paper.getComposeStrategy())) {
            List<ExamPaperQuestion> fixed = examPaperQuestionMapper.selectList(
                    Wrappers.<ExamPaperQuestion>lambdaQuery()
                            .eq(ExamPaperQuestion::getPaperId, paperId)
                            .orderByAsc(ExamPaperQuestion::getSortOrder));
            if (fixed.isEmpty()) {
                throw new BizException("固定试卷未配置题目");
            }
            snapshot = fixed.stream().map(f -> {
                ExamRecord.QuestionSnapshotItem item = new ExamRecord.QuestionSnapshotItem();
                item.setQuestionId(f.getQuestionId());
                item.setScore(f.getScore());
                return item;
            }).collect(Collectors.toList());
            List<Long> ids = snapshot.stream().map(ExamRecord.QuestionSnapshotItem::getQuestionId).toList();
            questions = questionMapper.selectBatchIds(ids);

        } else {
            snapshot = new ArrayList<>();
            questions = new ArrayList<>();
            drawByType(paper.getBankId(), "SINGLE", paper.getSingleCount(), paper.getSingleScore(), snapshot, questions);
            drawByType(paper.getBankId(), "MULTIPLE", paper.getMultipleCount(), paper.getMultipleScore(), snapshot, questions);
            drawByType(paper.getBankId(), "JUDGE", paper.getJudgeCount(), paper.getJudgeScore(), snapshot, questions);
            Collections.shuffle(snapshot);
        }

        ExamRecord record = new ExamRecord();
        record.setPaperId(paperId);
        record.setUserId(userId);
        record.setMode(paper.getMode());
        record.setQuestionSnapshot(snapshot);
        record.setStartTime(LocalDateTime.now());
        record.setStatus(RecordStatus.IN_PROGRESS.name());
        examRecordMapper.insert(record);

        Map<Long, Question> questionById = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        Map<Long, BigDecimal> scoreByQuestionId = snapshot.stream()
                .collect(Collectors.toMap(ExamRecord.QuestionSnapshotItem::getQuestionId,
                        ExamRecord.QuestionSnapshotItem::getScore));

        StartExamResponse resp = new StartExamResponse();
        resp.setRecordId(record.getId());
        resp.setDurationMinutes(paper.getDurationMinutes());
        resp.setStartTime(record.getStartTime());
        resp.setQuestions(snapshot.stream().map(s -> {
            Question q = questionById.get(s.getQuestionId());
            QuestionDisplayVO vo = new QuestionDisplayVO();
            vo.setQuestionId(q.getId());
            vo.setType(q.getType());
            vo.setStem(q.getStem());
            vo.setOptions(q.getOptions());
            vo.setScore(scoreByQuestionId.get(q.getId()));
            return vo;
        }).collect(Collectors.toList()));
        return resp;
    }

    /** 从题库里按题型随机抽 count 道已审核通过的题。数据量不大，直接查id全量在内存shuffle。 */
    private void drawByType(Long bankId, String type, Integer count, BigDecimal score,
                             List<ExamRecord.QuestionSnapshotItem> snapshot, List<Question> questionsOut) {
        if (count == null || count == 0) return;

        List<Question> candidates = questionMapper.selectList(
                Wrappers.<Question>lambdaQuery()
                        .eq(Question::getBankId, bankId)
                        .eq(Question::getType, type)
                        .eq(Question::getReviewStatus, ReviewStatus.APPROVED.name()));

        if (candidates.size() < count) {
            throw new BizException(String.format(
                    "题库[%s]已审核通过的%s题只有%d道，不足以抽取%d道，请先补充或审核更多题目",
                    bankId, type, candidates.size(), count));
        }

        Collections.shuffle(candidates);
        List<Question> picked = candidates.subList(0, count);
        for (Question q : picked) {
            ExamRecord.QuestionSnapshotItem item = new ExamRecord.QuestionSnapshotItem();
            item.setQuestionId(q.getId());
            item.setScore(score);
            snapshot.add(item);
        }
        questionsOut.addAll(picked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamResultVO submitExam(SubmitExamRequest request, Long userId) {
        ExamRecord record = examRecordMapper.selectById(request.getRecordId());
        if (record == null || !record.getUserId().equals(userId)) {
            throw new BizException("考试记录不存在");
        }
        if (!RecordStatus.IN_PROGRESS.name().equals(record.getStatus())) {
            throw new BizException("该考试已交卷，不能重复提交");
        }

        Map<Long, String> userAnswers = request.getAnswers() == null ? Map.of()
                : request.getAnswers().stream()
                        .collect(Collectors.toMap(AnswerItem::getQuestionId, AnswerItem::getAnswer, (a, b) -> b));

        List<Long> questionIds = record.getQuestionSnapshot().stream()
                .map(ExamRecord.QuestionSnapshotItem::getQuestionId).toList();
        Map<Long, Question> questionById = questionMapper.selectBatchIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        Map<Long, BigDecimal> scoreByQuestionId = record.getQuestionSnapshot().stream()
                .collect(Collectors.toMap(ExamRecord.QuestionSnapshotItem::getQuestionId,
                        ExamRecord.QuestionSnapshotItem::getScore));

        BigDecimal totalScore = BigDecimal.ZERO;
        List<ExamResultVO.AnswerResultItem> resultItems = new ArrayList<>();

        for (Long questionId : questionIds) {
            Question q = questionById.get(questionId);
            String userAnswer = userAnswers.getOrDefault(questionId, "");
            BigDecimal fullScore = scoreByQuestionId.get(questionId);

            Boolean correct = null;
            BigDecimal earnedScore = null;

            if (!"SHORT_ANSWER".equals(q.getType())) {
                correct = isCorrect(q.getType(), userAnswer, q.getAnswer());
                earnedScore = correct ? fullScore : BigDecimal.ZERO;
                totalScore = totalScore.add(earnedScore);

                if (!correct) {
                    wrongQuestionService.recordWrong(userId, questionId);
                }
            }

            ExamAnswerDetail detail = new ExamAnswerDetail();
            detail.setRecordId(record.getId());
            detail.setQuestionId(questionId);
            detail.setQuestionType(q.getType());
            detail.setUserAnswer(userAnswer);
            detail.setCorrectAnswer(q.getAnswer());
            detail.setIsCorrect(correct == null ? null : (correct ? 1 : 0));
            detail.setScore(earnedScore);
            detail.setAnsweredAt(LocalDateTime.now());
            examAnswerDetailMapper.insert(detail);

            ExamResultVO.AnswerResultItem item = new ExamResultVO.AnswerResultItem();
            item.setQuestionId(questionId);
            item.setStem(q.getStem());
            item.setUserAnswer(userAnswer);
            item.setCorrectAnswer(q.getAnswer());
            item.setCorrect(correct);
            item.setScore(earnedScore);
            item.setAnalysis(q.getAnalysis());
            resultItems.add(item);
        }

        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        boolean pass = totalScore.compareTo(paper.getPassScore()) >= 0;

        record.setStatus(RecordStatus.SUBMITTED.name());
        record.setSubmitTime(LocalDateTime.now());
        record.setScore(totalScore);
        record.setPassFlag(pass ? 1 : 0);
        examRecordMapper.updateById(record);

        ExamResultVO vo = new ExamResultVO();
        vo.setRecordId(record.getId());
        vo.setScore(totalScore);
        vo.setTotalScore(paper.getTotalScore());
        vo.setPass(pass);
        vo.setDetails(resultItems);
        return vo;
    }

    /** 单选：答案完全相等；多选：字母集合完全一致；判断：直接比较"对"/"错" */
    private boolean isCorrect(String type, String userAnswer, String correctAnswer) {
        if (userAnswer == null) userAnswer = "";
        if ("MULTIPLE".equals(type)) {
            Set<Character> userSet = toCharSet(userAnswer);
            Set<Character> correctSet = toCharSet(correctAnswer);
            return userSet.equals(correctSet);
        }
        return userAnswer.trim().equalsIgnoreCase(correctAnswer == null ? "" : correctAnswer.trim());
    }

    private Set<Character> toCharSet(String s) {
        Set<Character> set = new HashSet<>();
        if (s == null) return set;
        for (char c : s.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') set.add(c);
        }
        return set;
    }
}
