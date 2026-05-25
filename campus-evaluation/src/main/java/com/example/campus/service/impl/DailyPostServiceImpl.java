package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.PostTagRelation;
import com.example.campus.entity.Tag;
import com.example.campus.exception.BusinessException;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.PostTagRelationMapper;
import com.example.campus.service.DailyPostService;
import com.example.campus.service.TagService;
import com.example.campus.utils.SensitiveWordFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DailyPostServiceImpl extends ServiceImpl<DailyPostMapper, DailyPost> implements DailyPostService {

    @Autowired
    private TagService tagService;

    @Autowired
    private PostTagRelationMapper postTagRelationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional
    public boolean publishPost(DailyPost post) {
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new BusinessException(400, "分享内容不能为空");
        }

        post.setContent(sensitiveWordFilter.filter(post.getContent()));

        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setCollectCount(0);
        post.setViewCount(0);
        post.setHotScore(BigDecimal.ZERO);
        post.setStatus(1);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());

        boolean success = this.save(post);

        if (success && post.getTags() != null && !post.getTags().isEmpty()) {
            savePostTags(post.getPostId(), post.getTags());
        }

        return success;
    }

    private void savePostTags(Long postId, String tagsJson) {
        try {
            List<String> tagNames = objectMapper.readValue(tagsJson, new TypeReference<List<String>>(){});
            if (tagNames == null || tagNames.isEmpty()) {
                return;
            }

            List<Long> tagIds = tagService.findOrCreateTags(tagNames);

            for (Long tagId : tagIds) {
                PostTagRelation relation = new PostTagRelation();
                relation.setPostId(postId);
                relation.setTagId(tagId);
                postTagRelationMapper.insert(relation);

                tagService.incrementUseCount(tagId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Object> getDiscoverPosts(int page, int size, String sortBy) {
        Page<DailyPost> postPage = new Page<>(page, size);
        QueryWrapper<DailyPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        if ("hot".equals(sortBy)) {
            queryWrapper.orderByDesc("hot_score");
        } else {
            queryWrapper.orderByDesc("create_time");
        }

        Page<DailyPost> resultPage = this.page(postPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    public DailyPost getPostDetail(Long postId) {
        DailyPost post = this.getById(postId);
        if (post != null && post.getStatus() == 1) {
            incrementViewCount(postId);
            return post;
        }
        return null;
    }

    @Override
    public Map<String, Object> getMyPosts(Long userId, int page, int size) {
        Page<DailyPost> postPage = new Page<>(page, size);
        QueryWrapper<DailyPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", 1)
                .orderByDesc("create_time");

        Page<DailyPost> resultPage = this.page(postPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    public Map<String, Object> getPostsByTopic(Long topicId, int page, int size) {
        Page<DailyPost> postPage = new Page<>(page, size);
        QueryWrapper<DailyPost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .like("topic_ids", topicId)
                .orderByDesc("create_time");

        Page<DailyPost> resultPage = this.page(postPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    @Transactional
    public void incrementViewCount(Long postId) {
        DailyPost post = this.getById(postId);
        if (post != null) {
            post.setViewCount(post.getViewCount() + 1);
            post.setUpdateTime(LocalDateTime.now());
            this.updateById(post);
        }
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        DailyPost post = this.getById(postId);

        if (post == null || post.getStatus() == 0) {
            return false;
        }

        if (!post.getUserId().equals(userId)) {
            return false;
        }

        post.setStatus(0);
        post.setUpdateTime(LocalDateTime.now());

        return this.updateById(post);
    }

    private BigDecimal calculateHotScore(DailyPost post) {
        double baseScore = post.getLikeCount() * 1.0
                + post.getCommentCount() * 2.0
                + post.getCollectCount() * 3.0;

        long hoursSinceCreated = ChronoUnit.HOURS.between(
                post.getCreateTime(), LocalDateTime.now()
        );
        double decayFactor = 1.0 / Math.pow(1.0 + hoursSinceCreated / 24.0, 1.8);

        return BigDecimal.valueOf(baseScore * decayFactor).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
