package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.CommentLike;
import com.example.campus.entity.EvaluationComment;
import com.example.campus.repository.CommentLikeMapper;
import com.example.campus.repository.EvaluationCommentMapper;
import com.example.campus.service.CommentLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {

    @Autowired
    private EvaluationCommentMapper evaluationCommentMapper;

    @Override
    @Transactional
    public boolean toggleLike(Long commentId, Long userId) {
        QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);
        CommentLike existingLike = this.getOne(queryWrapper);

        if (existingLike != null) {
            this.remove(queryWrapper);
            decreaseLikeCount(commentId);
            return false;
        } else {
            CommentLike newLike = new CommentLike();
            newLike.setCommentId(commentId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            this.save(newLike);
            increaseLikeCount(commentId);
            return true;
        }
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        QueryWrapper<CommentLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    private void increaseLikeCount(Long commentId) {
        EvaluationComment comment = evaluationCommentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            evaluationCommentMapper.updateById(comment);
        }
    }

    private void decreaseLikeCount(Long commentId) {
        EvaluationComment comment = evaluationCommentMapper.selectById(commentId);
        if (comment != null && comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            evaluationCommentMapper.updateById(comment);
        }
    }
}
