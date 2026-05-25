package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.DailyPost;

import java.util.List;
import java.util.Map;

/**
 * 日常分享业务接口
 */
public interface DailyPostService extends IService<DailyPost> {

    /**
     * 发布日常帖子
     */
    boolean publishPost(DailyPost post);

    /**
     * 获取发现页帖子列表（推荐 feed 流）
     * @param page 页码
     * @param size 每页数量
     * @param sortBy 排序方式：hot-热门, latest-最新
     */
    Map<String, Object> getDiscoverPosts(int page, int size, String sortBy);

    /**
     * 获取帖子详情
     */
    DailyPost getPostDetail(Long postId);

    /**
     * 获取用户的帖子列表
     */
    Map<String, Object> getMyPosts(Long userId, int page, int size);


    /**
     * 获取话题下的帖子列表
     * @param topicId 话题ID
     * @param page 页码
     * @param size 每页数量
     */
    Map<String, Object> getPostsByTopic(Long topicId, int page, int size);

    /**
     * 增加浏览数
     */
    void incrementViewCount(Long postId);
    /**
     * 删除帖子（软删除）
     * @param postId 帖子ID
     * @param userId 用户ID（用于权限验证）
     * @return true-删除成功，false-无权删除或帖子不存在
     */
    boolean deletePost(Long postId, Long userId);
}