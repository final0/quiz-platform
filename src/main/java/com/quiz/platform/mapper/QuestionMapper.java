package com.quiz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.platform.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
