package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.PostTopic;
import com.example.campus.entity.TopicFollow;
import com.example.campus.repository.TopicFollowMapper;
import com.example.campus.repository.TopicMapper;
import com.example.campus.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 话题业务实现类
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, PostTopic> implements TopicService {

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private TopicFollowMapper topicFollowMapper;

    @Override
    public List<PostTopic> getTopicList(String sortBy, String keyword, Long userId) {
        QueryWrapper<PostTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        // 搜索关键词
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("name", keyword);
        }

        // 排序
        if ("hot".equals(sortBy)) {
            queryWrapper.orderByDesc("follow_count", "post_count");
        } else if ("new".equals(sortBy)) {
            queryWrapper.orderByDesc("create_time");
        } else if ("followed".equals(sortBy)) {
            // 已关注的话题需要先获取用户关注的话题ID
            if (userId != null) {
                List<Long> followedTopicIds = topicMapper.selectFollowedTopicIds(userId);
                if (followedTopicIds.isEmpty()) {
                    return Collections.emptyList();
                }
                queryWrapper.in("topic_id", followedTopicIds);
            } else {
                return Collections.emptyList();
            }
        } else {
            // 默认按热度排序
            queryWrapper.orderByDesc("follow_count", "post_count");
        }

        List<PostTopic> topics = this.list(queryWrapper);

        // 如果提供了用户ID，添加关注状态
        if (userId != null && !topics.isEmpty()) {
            List<Long> followedTopicIds = topicMapper.selectFollowedTopicIds(userId);
            Set<Long> followedSet = new HashSet<>(followedTopicIds);

            for (PostTopic topic : topics) {
                // 使用反射或添加临时字段来存储关注状态
                // 这里简化处理，实际应该在 VO 中添加 isFollowed 字段
            }
        }

        return topics;
    }

    @Override
    public PostTopic getTopicDetail(Long topicId, Long userId) {
        PostTopic topic = this.getById(topicId);

        if (topic != null && topic.getStatus() == 1) {
            // 如果提供了用户ID，可以添加关注状态等信息
            return topic;
        }

        return null;
    }

    @Override
    @Transactional
    public Map<String, Object> toggleFollow(Long topicId, Long userId) {
        PostTopic topic = this.getById(topicId);
        if (topic == null || topic.getStatus() == 0) {
            throw new RuntimeException("话题不存在");
        }

        QueryWrapper<TopicFollow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("topic_id", topicId).eq("user_id", userId);
        TopicFollow existingFollow = topicFollowMapper.selectOne(queryWrapper);

        Map<String, Object> result = new HashMap<>();

        if (existingFollow != null) {
            topicFollowMapper.delete(queryWrapper);
            topic.setFollowCount(Math.max(0, topic.getFollowCount() - 1));
            result.put("isFollowed", false);
        } else {
            TopicFollow newFollow = new TopicFollow();
            newFollow.setTopicId(topicId);
            newFollow.setUserId(userId);
            newFollow.setCreateTime(LocalDateTime.now());
            topicFollowMapper.insert(newFollow);
            topic.setFollowCount(topic.getFollowCount() + 1);
            result.put("isFollowed", true);
        }

        topic.setUpdateTime(LocalDateTime.now());
        this.updateById(topic);

        result.put("followCount", topic.getFollowCount());

        return result;
    }

    @Override
    public List<PostTopic> getHotTopics(int limit) {
        QueryWrapper<PostTopic> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("follow_count", "post_count")
                .last("LIMIT " + limit);

        return this.list(queryWrapper);
    }
}