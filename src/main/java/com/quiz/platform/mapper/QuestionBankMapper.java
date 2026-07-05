package com.quiz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.platform.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
}
