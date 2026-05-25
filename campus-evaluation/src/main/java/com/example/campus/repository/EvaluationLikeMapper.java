package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.EvaluationLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价点赞数据访问接口（Mapper）
 * 继承 BaseMapper 后，自动拥有增删改查方法
 */
@Mapper
public interface EvaluationLikeMapper extends BaseMapper<EvaluationLike> {
    // 这里暂时不需要写额外代码，MyBatis-Plus 会自动实现基础的 CRUD
}
