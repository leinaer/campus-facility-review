package com.example.campus.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationComment;
import com.example.campus.entity.User;
import com.example.campus.repository.EvaluationCommentMapper;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.UserNotificationService;
import com.example.campus.service.EvaluationCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价评论业务实现类
 */
@Service
public class EvaluationCommentServiceImpl extends ServiceImpl<EvaluationCommentMapper, EvaluationComment> implements EvaluationCommentService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserNotificationService notificationService;

    @Override
    public List<EvaluationComment> getByEvaluationId(Long evaluationId, String sortBy) {
        System.out.println("=== 获取评论列表 ===");
        System.out.println("evaluationId: " + evaluationId);
        System.out.println("sortBy: " + sortBy);

        QueryWrapper<EvaluationComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId)
                .eq("status", 1);

        if ("hot".equals(sortBy)) {
            System.out.println("使用热度排序: ORDER BY like_count DESC, create_time DESC");
            queryWrapper.orderByDesc("like_count", "create_time");
        } else {
            System.out.println("使用时间排序: ORDER BY create_time ASC");
            queryWrapper.orderByAsc("create_time");
        }

        List<EvaluationComment> comments = this.list(queryWrapper);
        System.out.println("查询到评论数量: " + comments.size());

        return comments;
    }


    @Override
    @Transactional
    public boolean publishComment(EvaluationComment comment) {
        // 设置默认值
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        // 如果 parentId 为空，设置为 0（一级评论）
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        // 保存评论
        boolean success = this.save(comment);

        if (success) {
            // 更新评价的评论数（只统计一级评论）
            if (comment.getParentId() == 0) {
                Evaluation evaluation = evaluationMapper.selectById(comment.getEvaluationId());
                if (evaluation != null) {
                    evaluation.setCommentCount(evaluation.getCommentCount() + 1);
                    evaluationMapper.updateById(evaluation);
                }
            }
            // 发送通知
            sendCommentNotification(comment);
        }

        return success;
    }

    private void sendCommentNotification(EvaluationComment comment) {
        try {
            Long currentUserId = comment.getUserId();

            // 1. 如果是一级评论，通知评价作者
            if (comment.getParentId() == 0 || comment.getParentId() == null) {
                Evaluation evaluation = evaluationMapper.selectById(comment.getEvaluationId());
                if (evaluation != null && !evaluation.getUserId().equals(currentUserId)) {
                    User currentUser = userMapper.selectById(currentUserId);
                    String userName = currentUser != null ? currentUser.getNickname() : "匿名用户";

                    notificationService.createNotification(
                            evaluation.getUserId(),
                            "COMMENT",
                            "有人评论了你的评价",
                            userName + " 评论了你的评价",
                            currentUserId,
                            comment.getEvaluationId(),
                            "EVALUATION"
                    );
                }
            }
            // 2. 如果是回复评论，通知被回复的用户
            else {
                if (comment.getReplyToUserId() != null && !comment.getReplyToUserId().equals(currentUserId)) {
                    User replyToUser = userMapper.selectById(comment.getReplyToUserId());
                    User currentUser = userMapper.selectById(currentUserId);
                    String userName = currentUser != null ? currentUser.getNickname() : "匿名用户";

                    notificationService.createNotification(
                            comment.getReplyToUserId(),
                            "REPLY",
                            "有人回复了你的评论",
                            userName + " 回复了你的评论",
                            currentUserId,
                            comment.getCommentId(),
                            "COMMENT"
                    );
                }

                // 3. 如果是回复评论，也通知评价作者（如果作者不是回复者且不是被回复者）
                Evaluation evaluation = evaluationMapper.selectById(comment.getEvaluationId());
                if (evaluation != null
                        && !evaluation.getUserId().equals(currentUserId)
                        && !evaluation.getUserId().equals(comment.getReplyToUserId())) {
                    User currentUser = userMapper.selectById(currentUserId);
                    String userName = currentUser != null ? currentUser.getNickname() : "匿名用户";

                    notificationService.createNotification(
                            evaluation.getUserId(),
                            "COMMENT",
                            "有人评论了你的评价",
                            userName + " 评论了你的评价",
                            currentUserId,
                            comment.getEvaluationId(),
                            "EVALUATION"
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public boolean deleteCommentById(Long commentId) {
        // 查询评论
        EvaluationComment comment = this.getById(commentId);
        if (comment == null) {
            return false;
        }

        Long evaluationId = comment.getEvaluationId();

        // 软删除评论
        comment.setStatus(0);
        comment.setUpdateTime(LocalDateTime.now());
        boolean success = this.updateById(comment);

        if (success) {
            // 减少评价的评论数（只减1，不重新计算，避免与 publishComment 冲突）
            Evaluation evaluation = evaluationMapper.selectById(evaluationId);
            if (evaluation != null && evaluation.getCommentCount() > 0) {
                evaluation.setCommentCount(evaluation.getCommentCount() - 1);
                evaluationMapper.updateById(evaluation);
            }
        }

        return success;
    }
}
