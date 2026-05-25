package com.example.campus.service;

/**
 * 评论投票服务接口
 */
public interface CommentVoteService {

    /**
     * 切换投票状态
     * @param commentId 评论ID
     * @param userId 用户ID
     * @param voteType 投票类型：1-点赞，0-点踩
     * @return 当前投票状态
     */
    boolean toggleVote(Long commentId, Long userId, Integer voteType);

    /**
     * 获取用户投票状态
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 投票类型：null-未投票，1-点赞，0-点踩
     */
    Integer getUserVoteType(Long commentId, Long userId);
}