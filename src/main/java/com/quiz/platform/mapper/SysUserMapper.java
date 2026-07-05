package com.quiz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quiz.platform.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
