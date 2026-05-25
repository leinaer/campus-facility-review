package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campus.entity.CommentLike;
import com.example.campus.entity.PostComment;
import com.example.campus.entity.User;
import com.example.campus.repository.CommentLikeMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子评论控制器
 */
@RestController
@RequestMapping("/api/post/comment")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布评论
     */
    @PostMapping("/publish")
    public Map<String, Object> publishComment(@RequestBody PostComment comment, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            comment.setUserId(Long.parseLong(userIdStr));
            boolean success = postCommentService.publishComment(comment);

            result.put("code", success ? 200 : 500);
            result.put("msg", success ? "评论成功" : "评论失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "评论失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取帖子的评论列表（扁平化，所有评论同级显示）
     */
    @GetMapping("/by-post/{postId}")
    public Map<String, Object> getComments(@PathVariable Long postId,
                                           @RequestParam(required = false, defaultValue = "time") String sortBy,
                                           HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PostComment> comments = postCommentService.getCommentsByPostId(postId, sortBy);
            
            String userIdStr = (String) request.getAttribute("userId");
            Long currentUserId = userIdStr != null ? Long.parseLong(userIdStr) : null;
            
            // 填充用户信息和点赞状态
            for (PostComment comment : comments) {
                // 填充评论者信息
                if (comment.getUserId() != null) {
                    User user = userMapper.selectById(comment.getUserId());
                    if (user != null) {
                        comment.setUserNickname(user.getNickname());
                        comment.setUserAvatar(user.getAvatarUrl());
                    }
                }
                
                // 填充被回复者信息
                if (comment.getReplyToUserId() != null) {
                    User replyUser = userMapper.selectById(comment.getReplyToUserId());
                    if (replyUser != null) {
                        comment.setReplyToUserName(replyUser.getNickname());
                    }
                }
                
                // 填充点赞状态
                if (currentUserId != null) {
                    QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("comment_id", comment.getCommentId())
                            .eq("user_id", currentUserId);
                    CommentLike like = commentLikeMapper.selectOne(queryWrapper);
                    comment.setIsLiked(like != null);
                } else {
                    comment.setIsLiked(false);
                }
            }
            
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", comments);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 切换评论点赞状态
     */
    @PostMapping("/like/toggle/{commentId}")
    public Map<String, Object> toggleLike(@PathVariable Long commentId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isLiked = postCommentService.toggleCommentLike(commentId, userId);

            PostComment comment = postCommentService.getById(commentId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("isLiked", isLiked);
            data.put("likeCount", comment != null ? comment.getLikeCount() : 0);
            
            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            PostComment comment = postCommentService.getById(commentId);

            if (comment == null) {
                result.put("code", 404);
                result.put("msg", "评论不存在");
                return result;
            }

            if (!comment.getUserId().equals(userId)) {
                result.put("code", 403);
                result.put("msg", "无权限删除");
                return result;
            }

            boolean success = postCommentService.deleteCommentById(commentId);
            result.put("code", success ? 200 : 500);
            result.put("msg", success ? "删除成功" : "删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }
}