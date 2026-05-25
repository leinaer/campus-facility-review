package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.EvaluationTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设施评价标签关联数据访问接口（Mapper）
 */
@Mapper
public interface EvaluationTagRelationMapper extends BaseMapper<EvaluationTagRelation> {
}
