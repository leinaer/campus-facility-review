package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.PostComment;

import java.util.List;

/**
 * 帖子评论业务接口
 */
public interface PostCommentService extends IService<PostComment> {

    /**
     * 发布评论
     */
    boolean publishComment(PostComment comment);

    /**
     * 获取帖子的评论列表
     * @param postId 帖子ID
     * @param sortBy 排序方式：time-时间，hot-热度
     * @return 评论列表（包含子评论）
     */
    List<PostComment> getCommentsByPostId(Long postId, String sortBy);

    /**
     * 切换评论点赞状态
     */
    boolean toggleCommentLike(Long commentId, Long userId);

    /**
     * 删除评论
     */
    boolean deleteCommentById(Long commentId);
}