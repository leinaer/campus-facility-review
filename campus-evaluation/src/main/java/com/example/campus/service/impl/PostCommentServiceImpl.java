package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.CommentLike;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.PostComment;
import com.example.campus.entity.User;
import com.example.campus.repository.CommentLikeMapper;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.PostCommentMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.PostCommentService;
import com.example.campus.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子评论业务实现类
 */
@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    @Autowired
    private DailyPostMapper dailyPostMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserNotificationService notificationService;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public boolean publishComment(PostComment comment) {
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return false;
        }

        comment.setLikeCount(0);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());

        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }

        boolean success = this.save(comment);

        if (success) {
            if (comment.getParentId() == 0) {
                incrementPostCommentCount(comment.getPostId());
            }
            
            sendCommentNotification(comment);
        }

        return success;
    }

    private void sendCommentNotification(PostComment comment) {
        try {
            Long currentUserId = comment.getUserId();
            
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                DailyPost post = dailyPostMapper.selectById(comment.getPostId());
                if (post != null && !post.getUserId().equals(currentUserId)) {
                    User currentUser = userMapper.selectById(currentUserId);
                    String userName = currentUser != null ? currentUser.getNickname() : "匿名用户";
                    
                    notificationService.createNotification(
                            post.getUserId(),
                            "COMMENT",
                            "有人评论了你的帖子",
                            userName + " 评论了你的帖子",
                            currentUserId,
                            comment.getPostId(),
                            "POST"
                    );
                }
            } 
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
                
                DailyPost post = dailyPostMapper.selectById(comment.getPostId());
                if (post != null 
                        && !post.getUserId().equals(currentUserId)
                        && !post.getUserId().equals(comment.getReplyToUserId())) {
                    User currentUser = userMapper.selectById(currentUserId);
                    String userName = currentUser != null ? currentUser.getNickname() : "匿名用户";
                    
                    notificationService.createNotification(
                            post.getUserId(),
                            "COMMENT",
                            "有人评论了你的帖子",
                            userName + " 评论了你的帖子",
                            currentUserId,
                            comment.getPostId(),
                            "POST"
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PostComment> getCommentsByPostId(Long postId, String sortBy) {
        QueryWrapper<PostComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId)
                .eq("status", 1);

        if ("hot".equals(sortBy)) {
            queryWrapper.orderByDesc("like_count", "create_time");
        } else {
            queryWrapper.orderByAsc("create_time");
        }

        // 直接返回所有评论，不分组
        return this.list(queryWrapper);
    }

    @Override
    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);
        CommentLike existingLike = commentLikeMapper.selectOne(queryWrapper);

        PostComment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (existingLike != null) {
            // 已点赞，取消点赞
            commentLikeMapper.delete(queryWrapper);
            
            // 重新计算点赞数，避免并发问题
            QueryWrapper<CommentLike> countWrapper = new QueryWrapper<>();
            countWrapper.eq("comment_id", commentId);
            long likeCount = commentLikeMapper.selectCount(countWrapper);
            
            comment.setLikeCount((int) likeCount);
            this.updateById(comment);
            
            return false;
        } else {
            // 未点赞，添加点赞
            CommentLike newLike = new CommentLike();
            newLike.setCommentId(commentId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            commentLikeMapper.insert(newLike);
            
            // 重新计算点赞数
            QueryWrapper<CommentLike> countWrapper = new QueryWrapper<>();
            countWrapper.eq("comment_id", commentId);
            long likeCount = commentLikeMapper.selectCount(countWrapper);
            
            comment.setLikeCount((int) likeCount);
            this.updateById(comment);
            
            return true;
        }
    }

    @Override
    @Transactional
    public boolean deleteCommentById(Long commentId) {
        PostComment comment = this.getById(commentId);
        if (comment == null) {
            return false;
        }

        comment.setStatus(0);
        boolean success = this.updateById(comment);

        if (success && (comment.getParentId() == null || comment.getParentId() == 0)) {
            decrementPostCommentCount(comment.getPostId());
        }

        return success;
    }

    private void incrementPostCommentCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }

    private void decrementPostCommentCount(Long postId) {
        DailyPost post = dailyPostMapper.selectById(postId);
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            post.setUpdateTime(LocalDateTime.now());
            dailyPostMapper.updateById(post);
        }
    }
}