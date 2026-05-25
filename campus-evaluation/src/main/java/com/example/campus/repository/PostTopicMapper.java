package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostTopic;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题数据访问接口（Mapper）
 */
@Mapper
public interface PostTopicMapper extends BaseMapper<PostTopic> {
}
