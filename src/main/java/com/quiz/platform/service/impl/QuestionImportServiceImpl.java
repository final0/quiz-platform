package com.quiz.platform.service.impl;

import com.quiz.platform.common.BizException;
import com.quiz.platform.common.enums.ImportStatus;
import com.quiz.platform.entity.ImportTask;
import com.quiz.platform.entity.Question;
import com.quiz.platform.entity.QuestionBank;
import com.quiz.platform.mapper.ImportTaskMapper;
import com.quiz.platform.mapper.QuestionBankMapper;
import com.quiz.platform.mapper.QuestionMapper;
import com.quiz.platform.parse.DocxRuleBasedParser;
import com.quiz.platform.parse.ParseResult;
import com.quiz.platform.parse.ParsedQuestion;
import com.quiz.platform.service.MinioStorageService;
import com.quiz.platform.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionImportServiceImpl implements QuestionImportService {

    private final MinioStorageService storageService;
    private final DocxRuleBasedParser docxRuleBasedParser;
    private final ImportTaskMapper importTaskMapper;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionMapper questionMapper;
    private final QuestionPersistenceService questionPersistenceService;
    private final com.quiz.platform.mapper.ImportAnomalyMapper importAnomalyMapper;

    @Override
    public Long importDocx(MultipartFile file, Long bankId, String parseMode, Long operatorId) {
        QuestionBank bank = questionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BizException("题库不存在: " + bankId);
        }

        String objectPath = storageService.upload(file, "question-bank/" + bankId);

        ImportTask task = new ImportTask();
        task.setBankId(bankId);
        task.setFileName(file.getOriginalFilename());
        task.setMinioPath(objectPath);
        task.setFileType("DOCX");
        task.setParseMode(parseMode);
        task.setStatus(ImportStatus.PROCESSING.name());
        task.setCreatorId(operatorId);
        importTaskMapper.insert(task);

        try {
            ParseResult result = doParse(file, parseMode);
            questionPersistenceService.persistQuestions(bankId, operatorId, result, task.getId());
            updateBankQuestionCount(bank);

            task.setStatus(ImportStatus.SUCCESS.name());
            task.setTotalCount(result.getQuestions().size());
            task.setHighConfCount((int) result.getQuestions().stream()
                    .filter(q -> q.getConfidence() == ParsedQuestion.Confidence.HIGH).count());
            task.setLowConfCount((int) result.countLowConfidence());
            task.setAnomalyCount(result.getAnomalies().size());
            task.setFinishedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);

            log.info("题库导入完成 taskId={} bankId={} {}", task.getId(), bankId, result.summary());
            return task.getId();

        } catch (Exception e) {
            log.error("题库导入解析失败 taskId={}", task.getId(), e);
            task.setStatus(ImportStatus.FAILED.name());
            task.setErrorMessage(e.getMessage());
            task.setFinishedAt(LocalDateTime.now());
            importTaskMapper.updateById(task);
            throw new BizException("解析失败: " + e.getMessage());
        }
    }

    private ParseResult doParse(MultipartFile file, String parseMode) throws Exception {
        if ("AI".equalsIgnoreCase(parseMode)) {
            throw new BizException("AI解析引擎待接入，当前请使用规则解析");
        }
        try (InputStream is = file.getInputStream()) {
            return docxRuleBasedParser.parse(is);
        }
    }

    @Override
    public com.quiz.platform.entity.ImportTask getTask(Long taskId) {
        com.quiz.platform.entity.ImportTask task = importTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException("导入任务不存在: " + taskId);
        }
        return task;
    }

    @Override
    public java.util.List<com.quiz.platform.entity.ImportAnomaly> listAnomalies(Long taskId) {
        return importAnomalyMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.quiz.platform.entity.ImportAnomaly>()
                        .eq(com.quiz.platform.entity.ImportAnomaly::getTaskId, taskId));
    }

    private void updateBankQuestionCount(QuestionBank bank) {
        List<Question> all = questionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Question>()
                        .eq(Question::getBankId, bank.getId()));
        bank.setSingleCount((int) all.stream().filter(q -> "SINGLE".equals(q.getType())).count());
        bank.setMultipleCount((int) all.stream().filter(q -> "MULTIPLE".equals(q.getType())).count());
        bank.setJudgeCount((int) all.stream().filter(q -> "JUDGE".equals(q.getType())).count());
        bank.setShortCount((int) all.stream().filter(q -> "SHORT_ANSWER".equals(q.getType())).count());
        questionBankMapper.updateById(bank);
    }
}
