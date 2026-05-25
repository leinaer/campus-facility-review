package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campus.dto.CommentVO;
import com.example.campus.entity.EvaluationComment;
import com.example.campus.entity.User;
import com.example.campus.repository.EvaluationCommentMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.CommentLikeService;
import com.example.campus.service.CommentVoteService;
import com.example.campus.service.EvaluationCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评价评论控制器
 */
@RestController
@RequestMapping("/api/comment")
public class EvaluationCommentController {

    @Autowired
    private EvaluationCommentService evaluationCommentService;

    @Autowired
    private CommentLikeService commentLikeService;

    @Autowired
    private CommentVoteService commentVoteService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EvaluationCommentMapper evaluationCommentMapper;

    @GetMapping("/by-evaluation/{evaluationId}")
    public Map<String, Object> getByEvaluation(@PathVariable Long evaluationId,
                                               @RequestParam(defaultValue = "time") String sortBy,
                                               HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            final Long currentUserId;
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                currentUserId = Long.parseLong(userIdStr);
            } else {
                currentUserId = null;
            }

            List<EvaluationComment> comments = evaluationCommentService.getByEvaluationId(evaluationId, sortBy);

            List<CommentVO> commentVOs = comments.stream()
                    .map(CommentVO::fromEntity)
                    .peek(vo -> {
                        if (vo.getUserId() != null) {
                            User user = userMapper.selectById(vo.getUserId());
                            if (user != null) {
                                vo.setUserNickname(user.getNickname());
                                vo.setUserAvatar(user.getAvatarUrl());
                            }
                        }
                        if (vo.getReplyToUserId() != null) {
                            User replyUser = userMapper.selectById(vo.getReplyToUserId());
                            if (replyUser != null) {
                                vo.setReplyToUserNickname(replyUser.getNickname());
                            }
                        }
                        if (currentUserId != null) {
                            vo.setIsLiked(commentLikeService.isLiked(vo.getCommentId(), currentUserId));
                            vo.setUserVoteType(commentVoteService.getUserVoteType(vo.getCommentId(), currentUserId));
                        } else {
                            vo.setIsLiked(false);
                            vo.setUserVoteType(null);
                        }

                    })
                    .collect(Collectors.toList());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", commentVOs);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }



    @PostMapping("/publish")
    public Map<String, Object> publishComment(@RequestBody EvaluationComment comment,
                                              HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            comment.setUserId(userId);

            boolean success = evaluationCommentService.publishComment(comment);
            if (success) {
                result.put("code", 200);
                result.put("msg", "评论成功");
            } else {
                result.put("code", 500);
                result.put("msg", "评论失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "评论失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteComment(@PathVariable Long id,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            EvaluationComment comment = evaluationCommentService.getById(id);
            if (comment != null && comment.getUserId().equals(userId)) {
                boolean success = evaluationCommentService.deleteCommentById(id);
                if (success) {
                    result.put("code", 200);
                    result.put("msg", "删除成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "删除失败");
                }
            } else {
                result.put("code", 403);
                result.put("msg", "无权限删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/like/toggle/{commentId}")
    public Map<String, Object> toggleLike(@PathVariable Long commentId,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            boolean isLiked = commentLikeService.toggleLike(commentId, userId);

            EvaluationComment comment = evaluationCommentMapper.selectById(commentId);

            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("isLiked", isLiked);
            result.put("likeCount", comment != null ? comment.getLikeCount() : 0);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/like/status/{commentId}")
    public Map<String, Object> getLikeStatus(@PathVariable Long commentId,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;

            boolean isLiked = false;
            if (userId != null) {
                isLiked = commentLikeService.isLiked(commentId, userId);
            }

            result.put("code", 200);
            result.put("isLiked", isLiked);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/vote/{commentId}")
    public Map<String, Object> vote(@PathVariable Long commentId,
                                    @RequestParam Integer voteType,
                                    HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);

            boolean isVoted = commentVoteService.toggleVote(commentId, userId, voteType);

            EvaluationComment comment = evaluationCommentMapper.selectById(commentId);

            Map<String, Object> data = new HashMap<>();
            data.put("isVoted", isVoted);
            data.put("likeCount", comment != null ? comment.getLikeCount() : 0);
            data.put("dislikeCount", comment != null ? comment.getDislikeCount() : 0);

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



    @GetMapping("/vote/status/{commentId}")
    public Map<String, Object> getVoteStatus(@PathVariable Long commentId,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;

            Integer voteType = null;
            if (userId != null) {
                voteType = commentVoteService.getUserVoteType(commentId, userId);
            }

            result.put("code", 200);
            result.put("voteType", voteType);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
        }
        return result;
    }
}
