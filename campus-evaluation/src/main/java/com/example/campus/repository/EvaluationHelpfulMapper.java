package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.EvaluationHelpful;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价有用性投票数据访问接口（Mapper）
 */
@Mapper
public interface EvaluationHelpfulMapper extends BaseMapper<EvaluationHelpful> {
}
