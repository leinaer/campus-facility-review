package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.PostTopic;

import java.util.List;
import java.util.Map;

/**
 * 话题业务接口
 */
public interface TopicService extends IService<PostTopic> {

    /**
     * 获取话题列表（支持筛选和搜索）
     * @param sortBy 排序方式：hot-热门, new-最新, followed-已关注
     * @param keyword 搜索关键词
     * @param userId 用户ID（用于判断是否已关注）
     */
    List<PostTopic> getTopicList(String sortBy, String keyword, Long userId);

    /**
     * 获取话题详情
     * @param topicId 话题ID
     * @param userId 用户ID（用于判断是否已关注）
     */
    PostTopic getTopicDetail(Long topicId, Long userId);

    /**
     * 切换话题关注状态
     * @param topicId 话题ID
     * @param userId 用户ID
     * @return 包含 isFollowed 和 followCount 的 Map
     */
    Map<String, Object> toggleFollow(Long topicId, Long userId);

    /**
     * 获取热门话题
     * @param limit 数量限制
     */
    List<PostTopic> getHotTopics(int limit);
}