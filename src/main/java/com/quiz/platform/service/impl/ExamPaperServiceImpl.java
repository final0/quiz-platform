package com.quiz.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quiz.platform.entity.ExamPaper;
import com.quiz.platform.mapper.ExamPaperMapper;
import com.quiz.platform.service.ExamPaperService;
import org.springframework.stereotype.Service;

@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements ExamPaperService {
}
