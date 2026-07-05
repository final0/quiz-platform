package com.quiz.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.platform.entity.QuestionBank;
import com.quiz.platform.mapper.QuestionBankMapper;
import com.quiz.platform.service.QuestionBankService;
import org.springframework.stereotype.Service;

@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {
}
