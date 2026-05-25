package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.TopicFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题关注数据访问接口（Mapper）
 */
@Mapper
public interface TopicFollowMapper extends BaseMapper<TopicFollow> {
}