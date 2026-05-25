package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.PostLike;

/**
 * 帖子点赞业务接口
 */
public interface PostLikeService extends IService<PostLike> {

    /**
     * 切换点赞状态
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return true-已点赞，false-已取消点赞
     */
    boolean toggleLike(Long postId, Long userId);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long postId, Long userId);
}