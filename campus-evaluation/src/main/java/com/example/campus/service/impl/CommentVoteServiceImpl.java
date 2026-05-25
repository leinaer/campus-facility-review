package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.CommentVote;
import com.example.campus.entity.EvaluationComment;
import com.example.campus.repository.CommentVoteMapper;
import com.example.campus.repository.EvaluationCommentMapper;
import com.example.campus.service.CommentVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评论投票服务实现
 */
@Service
public class CommentVoteServiceImpl extends ServiceImpl<CommentVoteMapper, CommentVote> implements CommentVoteService {

    @Autowired
    private EvaluationCommentMapper evaluationCommentMapper;

    @Override
    @Transactional
    public boolean toggleVote(Long commentId, Long userId, Integer voteType) {
        QueryWrapper<CommentVote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);
        CommentVote existVote = this.getOne(queryWrapper);

        EvaluationComment comment = evaluationCommentMapper.selectById(commentId);
        if (comment == null) {
            return false;
        }

        if (existVote != null) {
            Integer oldVoteType = existVote.getVoteType();
            if (oldVoteType.equals(voteType)) {
                this.remove(queryWrapper);
                updateVoteCount(commentId, voteType, -1);
                return false;
            } else {
                existVote.setVoteType(voteType);
                this.updateById(existVote);
                updateVoteCount(commentId, oldVoteType, -1);
                updateVoteCount(commentId, voteType, 1);
                return true;
            }
        } else {
            CommentVote newVote = new CommentVote();
            newVote.setCommentId(commentId);
            newVote.setUserId(userId);
            newVote.setVoteType(voteType);
            newVote.setCreateTime(LocalDateTime.now());
            this.save(newVote);
            updateVoteCount(commentId, voteType, 1);
            return true;
        }
    }

    @Override
    public Integer getUserVoteType(Long commentId, Long userId) {
        QueryWrapper<CommentVote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", commentId)
                .eq("user_id", userId);
        CommentVote vote = this.getOne(queryWrapper);
        return vote != null ? vote.getVoteType() : null;
    }

    private void updateVoteCount(Long commentId, Integer voteType, int delta) {
        EvaluationComment comment = evaluationCommentMapper.selectById(commentId);
        if (comment != null) {
            if (voteType == 1) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
            } else {
                if (comment.getDislikeCount() == null) {
                    comment.setDislikeCount(0);
                }
                comment.setDislikeCount(Math.max(0, comment.getDislikeCount() + delta));
            }
            evaluationCommentMapper.updateById(comment);
        }
    }
}