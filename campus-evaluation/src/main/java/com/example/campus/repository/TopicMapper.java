package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 话题数据访问接口（Mapper）
 */
@Mapper
public interface TopicMapper extends BaseMapper<PostTopic> {

    /**
     * 查询用户关注的话题ID列表
     */
    @Select("SELECT topic_id FROM topic_follow WHERE user_id = #{userId}")
    List<Long> selectFollowedTopicIds(@Param("userId") Long userId);

    /**
     * 检查用户是否已关注话题
     */
    @Select("SELECT COUNT(*) FROM topic_follow WHERE topic_id = #{topicId} AND user_id = #{userId}")
    int countFollow(@Param("topicId") Long topicId, @Param("userId") Long userId);
}